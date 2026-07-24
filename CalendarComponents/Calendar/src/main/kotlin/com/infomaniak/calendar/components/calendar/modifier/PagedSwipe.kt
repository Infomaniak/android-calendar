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
package com.infomaniak.calendar.components.calendar.modifier

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

private val FAST_SWIPE_VELOCITY = 300.dp
private const val NO_PAGE_CHANGE = 0
private const val NEXT_PAGE = 1
private const val PREVIOUS_PAGE = -1

/**
 * Turns horizontal drags into one-page-at-a-time navigation, reporting the destination page as soon
 * as the finger lifts.
 *
 * Paging libraries usually decide where to land during the fling and never publish that decision,
 * so the only way to know is to read the layout back once everything has settled. Handling the
 * gesture here means [onPageSwiped] fires before any animation starts, which matters when a swipe
 * is meant to drive the rest of the screen and not just the pager.
 *
 * The snap rule matches the one Compose uses, so the layout still behaves like a standard pager: a
 * swipe released above [FAST_SWIPE_VELOCITY] moves one page in that direction whatever the distance
 * covered, otherwise the page only changes once the drag passes the halfway mark.
 *
 * The scrolling layout must have its own gesture handling disabled, or it will fight this one.
 *
 * @param scrollableState the layout being scrolled, driven directly by this modifier.
 * @param displayedPage the page currently on screen.
 * @param onPageSwiped called with the page the swipe started from and the direction ([NEXT_PAGE] or
 * [PREVIOUS_PAGE]), before any animation runs. Must return the page being navigated to.
 * @param animateScrollToPage moves the layout to the given page once the finger is lifted.
 */
internal fun <P> Modifier.pagedSwipe(
    scrollableState: ScrollableState,
    displayedPage: () -> P,
    onPageSwiped: (from: P, pageOffset: Int) -> P,
    animateScrollToPage: suspend (P) -> Unit,
): Modifier = this then PagedSwipeElement(scrollableState, displayedPage, onPageSwiped, animateScrollToPage)

private data class PagedSwipeElement<P>(
    private val scrollableState: ScrollableState,
    private val displayedPage: () -> P,
    private val onPageSwiped: (P, Int) -> P,
    private val animateScrollToPage: suspend (P) -> Unit,
) : ModifierNodeElement<PagedSwipeNode<P>>() {

    override fun create() = PagedSwipeNode(scrollableState, displayedPage, onPageSwiped, animateScrollToPage)

    override fun update(node: PagedSwipeNode<P>) {
        node.update(scrollableState, displayedPage, onPageSwiped, animateScrollToPage)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "pagedSwipe"
        properties["scrollableState"] = scrollableState
    }
}

private class PagedSwipeNode<P>(
    private var scrollableState: ScrollableState,
    private var displayedPage: () -> P,
    private var onPageSwiped: (P, Int) -> P,
    private var animateScrollToPage: suspend (P) -> Unit,
) : DelegatingNode(), PointerInputModifierNode {

    private var pageWidth = 0
    private var releaseVelocity = 0f
    private val velocityTracker = VelocityTracker()
    private var animatingToPage: P? = null

    /**
     * Bridges the pointer callbacks, which can't suspend, to the scroll session, which stays open
     * for the whole gesture. Closing it is how the session learns the finger was lifted.
     *
     * This is the same approach Compose itself takes in `DragGestureNode`, behind `draggable` and
     * `scrollable`.
     */
    private var dragDeltas: Channel<Float>? = null

    private val pointerInput = delegate(
        SuspendingPointerInputModifierNode {
            detectHorizontalDragGestures(
                onDragStart = { onSwipeStarted() },
                onDragEnd = { onSwipeReleased(velocityTracker.calculateVelocity().x) },
                onDragCancel = { onSwipeReleased(releaseVelocity = 0f) },
            ) { change, dragAmount ->
                velocityTracker.addPosition(change.uptimeMillis, change.position)
                dragDeltas?.trySend(dragAmount)
                change.consume()
            }
        },
    )

    fun update(
        scrollableState: ScrollableState,
        displayedPage: () -> P,
        onPageSwiped: (P, Int) -> P,
        animateScrollToPage: suspend (P) -> Unit,
    ) {
        if (this.scrollableState != scrollableState) {
            this.scrollableState = scrollableState
            abortSwipe()
            pointerInput.resetPointerInputHandler()
        }
        this.displayedPage = displayedPage
        this.onPageSwiped = onPageSwiped
        this.animateScrollToPage = animateScrollToPage
    }

    override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
        pageWidth = bounds.width
        pointerInput.onPointerEvent(pointerEvent, pass, bounds)
    }

    override fun onCancelPointerInput() = pointerInput.onCancelPointerInput()

    override fun onDetach() = abortSwipe()

    private fun abortSwipe() {
        dragDeltas?.close()
        dragDeltas = null
        animatingToPage = null
    }

    private fun onSwipeStarted() {
        val deltas = Channel<Float>(Channel.UNLIMITED)
        dragDeltas?.close()
        dragDeltas = deltas

        velocityTracker.resetTracking()
        releaseVelocity = 0f

        val startPage = swipeStartPage()
        val fastSwipeVelocity = with(requireDensity()) { FAST_SWIPE_VELOCITY.toPx() }

        coroutineScope.launch {
            val draggedDistance = consumeDrag(deltas)
            val pageOffset = pageOffsetFor(draggedDistance, fastSwipeVelocity)

            val targetPage = if (pageOffset == NO_PAGE_CHANGE) startPage else onPageSwiped(startPage, pageOffset)
            animatingToPage = targetPage

            animateScrollToPage(targetPage)

            animatingToPage = null
        }
    }

    private fun onSwipeReleased(releaseVelocity: Float) {
        this.releaseVelocity = releaseVelocity
        dragDeltas?.close()
    }

    /**
     * Chained swipes start from the page the previous one was heading to: the displayed page only
     * changes past the halfway mark, so a quick second swipe would otherwise report a page change
     * that already happened.
     */
    private fun swipeStartPage(): P = animatingToPage?.takeIf { scrollableState.isScrollInProgress } ?: displayedPage()

    private suspend fun consumeDrag(deltas: Channel<Float>): Float {
        var draggedDistance = 0f
        scrollableState.scroll(MutatePriority.UserInput) {
            for (delta in deltas) {
                draggedDistance -= scrollBy(-delta)
            }
        }
        return draggedDistance
    }

    private fun pageOffsetFor(draggedDistance: Float, fastSwipeVelocity: Float): Int {
        val isFastSwipe = abs(releaseVelocity) >= fastSwipeVelocity
        val hasCrossedHalfPage = abs(draggedDistance) > pageWidth / 2f

        return when {
            isFastSwipe -> if (releaseVelocity < 0) NEXT_PAGE else PREVIOUS_PAGE
            hasCrossedHalfPage -> if (draggedDistance < 0) NEXT_PAGE else PREVIOUS_PAGE
            else -> NO_PAGE_CHANGE
        }
    }
}
