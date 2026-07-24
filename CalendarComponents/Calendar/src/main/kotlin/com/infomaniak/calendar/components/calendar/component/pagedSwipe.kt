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
 *
 * ### Settling exactly on a page
 * Once the finger lifts and the destination page is known, the settle is delegated to
 * [animateScrollToPage], the calendar library's own animated scroll to a page. It always lands the
 * pager exactly on that page (leading offset 0), so repeated swipes can never accumulate a
 * fraction-of-a-pixel drift. Driving the settle by hand instead — animating a *relative* distance
 * and correcting the leftover afterwards — left every swipe a spring-epsilon off, and those errors
 * piled up into a visible, self-correcting jitter when swipes were spammed.
 *
 * Use with `userScrollEnabled = false` so the list never competes for the gesture.
 *
 * @param state the calendar's scrollable state, scrolled directly by this modifier during the drag.
 * @param currentPage the page currently displayed, read only when no swipe is pending.
 * @param onPageChange called with the page the gesture started from and the step (-1 or 1), before
 * any animation runs. Must return the resulting page. Not called when the swipe doesn't change page.
 * @param animateScrollToPage animates the pager to the given page and lands exactly on it; used to
 * settle once the destination is known.
 */
internal fun <P> Modifier.pagedSwipe(
    state: ScrollableState,
    currentPage: () -> P,
    onPageChange: (from: P, step: Int) -> P,
    animateScrollToPage: suspend (P) -> Unit,
): Modifier = this then PagedSwipeElement(state, currentPage, onPageChange, animateScrollToPage)

/** Same threshold as Compose's `SnapFlingBehavior`, so a flick feels identical to a system pager. */
private val MIN_FLING_VELOCITY = 300.dp

private data class PagedSwipeElement<P>(
    private val state: ScrollableState,
    private val currentPage: () -> P,
    private val onPageChange: (P, Int) -> P,
    private val animateScrollToPage: suspend (P) -> Unit,
) : ModifierNodeElement<PagedSwipeNode<P>>() {

    override fun create() = PagedSwipeNode(state, currentPage, onPageChange, animateScrollToPage)

    override fun update(node: PagedSwipeNode<P>) {
        node.update(state, currentPage, onPageChange, animateScrollToPage)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "pagedSwipe"
        properties["state"] = state
    }
}

private class PagedSwipeNode<P>(
    private var state: ScrollableState,
    private var currentPage: () -> P,
    private var onPageChange: (P, Int) -> P,
    private var animateScrollToPage: suspend (P) -> Unit,
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
     *
     * It is only trusted while that settle is still in progress (see [startGesture]); once it has
     * finished the displayed page is authoritative again, so a selection made outside a swipe in the
     * meantime is picked up instead of this now-stale destination.
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
        currentPage: () -> P,
        onPageChange: (P, Int) -> P,
        animateScrollToPage: suspend (P) -> Unit,
    ) {
        // A new state means a different calendar: abort the gesture rather than scroll the old one.
        if (this.state != state) {
            this.state = state
            deltas?.close()
            pendingPage = null
            pointerInput.resetPointerInputHandler()
        }
        this.currentPage = currentPage
        this.onPageChange = onPageChange
        this.animateScrollToPage = animateScrollToPage
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

        // Trust the pending destination only while its settle is still running (a chained swipe);
        // once it has finished the displayed page is authoritative again, so an external selection
        // that happened in between is honoured instead of a stale destination.
        val from = pendingPage?.takeIf { state.isScrollInProgress } ?: currentPage()
        val minFlingVelocity = with(requireDensity()) { MIN_FLING_VELOCITY.toPx() }

        coroutineScope.launch {
            // The drag owns a single scroll session: deltas are consumed until `onDragEnd`/
            // `onDragCancel` closes the channel, so no delta is still in flight when the loop exits
            // and the snap decision below sees the complete gesture. `dragged` accumulates what the
            // list actually took, which is less than the finger travel at the range edges, so the
            // distance test stays truthful there too.
            var dragged = 0f
            state.scroll(MutatePriority.UserInput) {
                for (delta in channel) {
                    dragged -= scrollBy(-delta)
                }
            }

            val step = when {
                abs(endVelocity) >= minFlingVelocity -> if (endVelocity < 0) 1 else -1
                abs(dragged) > width / 2f -> if (dragged < 0) 1 else -1
                else -> 0
            }

            // Destination page: the neighbour when the swipe commits, otherwise the page we started
            // on (snap back). `onPageChange` fires before the settle animation — the whole point of
            // handling the gesture by hand — and only when the page actually changes.
            val target = if (step != 0) onPageChange(from, step) else from
            pendingPage = target

            // Settle by delegating to the library's animated scroll, which lands the pager exactly
            // on `target` (leading offset 0). No relative distance to compute and no leftover to
            // correct, so the alignment can't drift no matter how fast swipes are chained. A chained
            // swipe's `UserInput` scroll pre-empts this animation and keeps `pendingPage` as its
            // start, so the reset below is skipped until a settle finishes uninterrupted.
            animateScrollToPage(target)

            // Settled without interruption: the displayed page is authoritative again.
            pendingPage = null
        }
    }
}
