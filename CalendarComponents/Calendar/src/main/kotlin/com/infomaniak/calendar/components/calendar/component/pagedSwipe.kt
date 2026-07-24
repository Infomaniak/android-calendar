/*
 * Infomaniak Calendar - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.calendar.components.calendar.component

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Drives a one-page-per-swipe gesture over a [ScrollableState], replacing the calendar library's
 * own paged fling.
 *
 * ### Why not use the library's paging
 * With `calendarScrollPaged`, the destination page is chosen by Compose's `SnapFlingBehavior`
 * during the fling, and that decision is never published: the only way to learn where the calendar
 * landed is to read the layout back once everything has settled. That costs a full fling plus snap
 * animation before the rest of the UI can react, which is very visible when a swipe is supposed to
 * move the whole planning to another month. Here the gesture is handled directly, so the target
 * page is known the moment the finger lifts and [onPageChange] can fire before anything animates.
 *
 * ### Snap rule
 * Mirrors what Compose does, so the calendar still feels like a standard pager: a flick faster than
 * [MIN_FLING_VELOCITY] moves one page in the direction of the flick, whatever the distance covered;
 * below that threshold, the page that is closest wins, i.e. the swipe must pass the halfway mark.
 * The settle animation reuses the gesture velocity with the spring the library's snap uses, so the
 * motion stays continuous with the finger.
 *
 * Use with `userScrollEnabled = false` so the list never competes for the gesture.
 *
 * @param state the calendar's scrollable state, scrolled directly by this modifier.
 * @param firstVisibleItemOffset offset in px of the leading item, used to re-align pages exactly
 * once the spring has settled.
 * @param currentPage the page currently displayed, read only when no swipe is pending.
 * @param onPageChange called with the page the gesture started from and the step (-1 or 1), before
 * any animation runs. Must return the resulting page. Not called when the swipe doesn't change page.
 */
internal fun <P> Modifier.pagedSwipe(
    state: ScrollableState,
    firstVisibleItemOffset: () -> Int,
    currentPage: () -> P,
    onPageChange: (from: P, step: Int) -> P,
): Modifier = this then PagedSwipeElement(state, firstVisibleItemOffset, currentPage, onPageChange)

/** Same threshold as Compose's `SnapFlingBehavior`, so a flick feels identical to a system pager. */
private val MIN_FLING_VELOCITY = 300.dp

private data class PagedSwipeElement<P>(
    private val state: ScrollableState,
    private val firstVisibleItemOffset: () -> Int,
    private val currentPage: () -> P,
    private val onPageChange: (P, Int) -> P,
) : ModifierNodeElement<PagedSwipeNode<P>>() {

    override fun create() = PagedSwipeNode(state, firstVisibleItemOffset, currentPage, onPageChange)

    override fun update(node: PagedSwipeNode<P>) {
        node.update(state, firstVisibleItemOffset, currentPage, onPageChange)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "pagedSwipe"
        properties["state"] = state
    }
}

private class PagedSwipeNode<P>(
    private var state: ScrollableState,
    private var firstVisibleItemOffset: () -> Int,
    private var currentPage: () -> P,
    private var onPageChange: (P, Int) -> P,
) : DelegatingNode(), PointerInputModifierNode {

    /** Viewport width in px, which is also the page width. Kept up to date from pointer events. */
    private var width = 0

    /** Velocity at the end of the gesture, read by the settle coroutine once the channel closes. */
    private var endVelocity = 0f

    /**
     * Carries drag deltas to the scroll session. Closing it is what tells the session the gesture
     * is over, so a gesture is exactly one channel lifetime.
     */
    private var deltas: Channel<Float>? = null

    private val velocityTracker = VelocityTracker()

    /**
     * Page the last swipe targeted, while its settle animation is still running.
     *
     * Chained swipes must start from it rather than from [currentPage]: the displayed page only
     * flips past the halfway point, so a fast second swipe would otherwise read the page the first
     * one started from, report a change that already happened, and leave the selection stuck.
     */
    private var pendingPage: P? = null

    private val pointerInput = delegate(
        SuspendingPointerInputModifierNode {
            detectHorizontalDragGestures(
                onDragStart = { startGesture() },
                onDragEnd = {
                    // Compute the velocity before closing: the session needs it to decide the step.
                    endVelocity = velocityTracker.calculateVelocity().x
                    deltas?.close()
                },
                onDragCancel = {
                    // No velocity, so the settle falls back to the distance rule and usually
                    // snaps back to the page the gesture started on.
                    endVelocity = 0f
                    deltas?.close()
                },
            ) { change, dragAmount ->
                velocityTracker.addPosition(change.uptimeMillis, change.position)
                deltas?.trySend(dragAmount)
                change.consume()
            }
        },
    )

    fun update(
        state: ScrollableState,
        firstVisibleItemOffset: () -> Int,
        currentPage: () -> P,
        onPageChange: (P, Int) -> P,
    ) {
        // A new state means a different calendar: abort the gesture rather than scroll the old one.
        if (this.state != state) {
            this.state = state
            deltas?.close()
            pendingPage = null
            pointerInput.resetPointerInputHandler()
        }
        this.firstVisibleItemOffset = firstVisibleItemOffset
        this.currentPage = currentPage
        this.onPageChange = onPageChange
    }

    override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
        // `bounds` is the only place the node sees its size, hence reading the page width here.
        width = bounds.width
        pointerInput.onPointerEvent(pointerEvent, pass, bounds)
    }

    override fun onCancelPointerInput() = pointerInput.onCancelPointerInput()

    override fun onDetach() {
        // Otherwise the settle coroutine stays suspended on a channel nobody will ever close.
        deltas?.close()
    }

    private fun startGesture() {
        val channel = Channel<Float>(Channel.UNLIMITED)
        deltas?.close()
        deltas = channel

        velocityTracker.resetTracking()
        endVelocity = 0f

        val from = pendingPage ?: currentPage()
        val minFlingVelocity = with(requireDensity()) { MIN_FLING_VELOCITY.toPx() }

        coroutineScope.launch {
            // A single scroll session owns the whole gesture, drag and settle alike. Launching one
            // coroutine per drag event instead let the last delta land *after* the settle distance
            // had been computed, leaving the calendar a page-fraction off for the duration of the
            // animation until a final snap yanked it back into place.
            state.scroll(MutatePriority.UserInput) {
                // Consume deltas until `onDragEnd`/`onDragCancel` closes the channel. `dragged`
                // accumulates what the list actually took, which is less than the finger travel at
                // the range edges, so the settle distance below stays truthful.
                var dragged = 0f
                for (delta in channel) {
                    dragged -= scrollBy(-delta)
                }

                val step = when {
                    abs(endVelocity) >= minFlingVelocity -> if (endVelocity < 0) 1 else -1
                    abs(dragged) > width / 2f -> if (dragged < 0) 1 else -1
                    else -> 0
                }

                // Fire before animating: this is the whole point of handling the gesture manually.
                if (step != 0) pendingPage = onPageChange(from, step)

                // One page spans the viewport, so what remains is the page width minus the part the
                // finger already covered. `dragged` is negative when swiping forward, hence the sum.
                var last = 0f
                AnimationState(initialValue = 0f, initialVelocity = -endVelocity).animateTo(
                    targetValue = width * step + dragged,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                ) {
                    // `animateTo` exposes an absolute value while `scrollBy` takes deltas.
                    scrollBy(value - last)
                    last = value
                }

                // Springs stop within an epsilon of their target, and those epsilons would pile up
                // over many swipes until pages sat visibly off-grid. Snap the leftover to whichever
                // page edge is nearest.
                val offset = firstVisibleItemOffset()
                val correction = if (-offset <= width / 2) offset else offset + width
                if (correction != 0) scrollBy(correction.toFloat())

                // Settled: the displayed page is authoritative again, chaining is over.
                pendingPage = null
            }
        }
    }
}
