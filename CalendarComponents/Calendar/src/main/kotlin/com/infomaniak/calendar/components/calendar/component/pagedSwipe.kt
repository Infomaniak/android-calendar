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
 * Drives a one-page-per-swipe gesture, replacing the calendar library's paged fling so the
 * destination page is known at drag end, before the settle animation starts.
 *
 * Mirrors Compose's own snap rule: a flick past [MIN_FLING_VELOCITY] wins on velocity,
 * otherwise the closest page wins on distance. The settle reuses the gesture velocity with
 * the spring the library's snap uses, so the motion stays continuous with the finger.
 *
 * Use with `userScrollEnabled = false` so the list never competes for the gesture.
 *
 * @param currentPage the page currently displayed, read only when no swipe is pending.
 * @param onPageChange called with the page the gesture started from and the step (-1 or 1),
 * before any animation runs. Must return the resulting page. Not called when the swipe
 * doesn't change page.
 */
internal fun <P> Modifier.pagedSwipe(
    state: ScrollableState,
    firstVisibleItemOffset: () -> Int,
    currentPage: () -> P,
    onPageChange: (from: P, step: Int) -> P,
): Modifier = this then PagedSwipeElement(state, firstVisibleItemOffset, currentPage, onPageChange)

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

    private var width = 0
    private var endVelocity = 0f
    private var deltas: Channel<Float>? = null
    private val velocityTracker = VelocityTracker()

    /**
     * Page the last swipe targeted, while its settle animation is still running.
     *
     * Chained swipes must start from it rather than from [currentPage]: the displayed page
     * only flips past the halfway point, so a fast second swipe would otherwise read the
     * page the first one started from and report a change that already happened.
     */
    private var pendingPage: P? = null

    private val pointerInput = delegate(
        SuspendingPointerInputModifierNode {
            detectHorizontalDragGestures(
                onDragStart = { startGesture() },
                onDragEnd = {
                    endVelocity = velocityTracker.calculateVelocity().x
                    deltas?.close()
                },
                onDragCancel = {
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
        width = bounds.width
        pointerInput.onPointerEvent(pointerEvent, pass, bounds)
    }

    override fun onCancelPointerInput() = pointerInput.onCancelPointerInput()

    override fun onDetach() {
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
            // A single scroll session owns the whole gesture. One coroutine per drag event
            // would let the last delta land after the settle distance was computed, leaving
            // the calendar a few dozen pixels off until a final snap yanked it back.
            state.scroll(MutatePriority.UserInput) {
                var dragged = 0f
                for (delta in channel) dragged -= scrollBy(-delta)

                val step = when {
                    abs(endVelocity) >= minFlingVelocity -> if (endVelocity < 0) 1 else -1
                    abs(dragged) > width / 2f -> if (dragged < 0) 1 else -1
                    else -> 0
                }

                // Fire before animating: the destination is already known.
                if (step != 0) pendingPage = onPageChange(from, step)

                var last = 0f
                AnimationState(initialValue = 0f, initialVelocity = -endVelocity).animateTo(
                    targetValue = width * step + dragged,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                ) {
                    scrollBy(value - last)
                    last = value
                }

                // Absorb the spring's residual so pages stay exactly aligned over time.
                val offset = firstVisibleItemOffset()
                val correction = if (-offset <= width / 2) offset else offset + width
                if (correction != 0) scrollBy(correction.toFloat())

                // Settled: the displayed page is authoritative again.
                pendingPage = null
            }
        }
    }
}
