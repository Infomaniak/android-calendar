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
package com.infomaniak.calendar.components.event.highlight

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.findNearestAncestor
import androidx.compose.ui.node.requireLayoutCoordinates
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi

/** How far the finger pulls the held card before it comes loose and can be dropped elsewhere. */
private val DetachDistance = 40.dp

/**
 * Holds [event] up in [state] for as long as a long press on its card, which [key] identifies, lasts.
 *
 * Until the long press is recognised, the gesture belongs to whoever claims it: a tap still opens the
 * event, and a scroll, a swipe or a pinch that starts first cancels the long press. Once recognised,
 * the gesture is the highlight's own: every change is consumed, ahead of the card's click and the
 * timeline's scrolling, until the finger lifts or [releaseHighlightOnSecondPointer] lets the event go.
 *
 * When [state] takes drops, moving the finger pulls the card, until it comes loose and is dragged, drawn by
 * [draggedCard], to be dropped where the finger lifts.
 */
internal fun Modifier.highlightOnLongPress(
    state: EventHighlightState,
    event: EventSummaryUi.Normal,
    key: Any,
    draggedCard: (@Composable (Modifier) -> Unit)?,
): Modifier {
    return this then LongPressHighlightElement(state, event, key, draggedCard)
}

private data class LongPressHighlightElement(
    val state: EventHighlightState,
    val event: EventSummaryUi.Normal,
    val key: Any,
    val draggedCard: (@Composable (Modifier) -> Unit)?,
) : ModifierNodeElement<LongPressHighlightNode>() {
    override fun create() = LongPressHighlightNode(state, event, key, draggedCard)
    override fun update(node: LongPressHighlightNode) = node.update(state, event, key, draggedCard)
}

private class LongPressHighlightNode(
    private var state: EventHighlightState,
    private var event: EventSummaryUi.Normal,
    private var key: Any,
    // Only read once held, so a new lambda on every recomposition doesn't restart the gesture.
    private var draggedCard: (@Composable (Modifier) -> Unit)?,
) : DelegatingNode(), CompositionLocalConsumerModifierNode {

    private val pointerInput = delegate(SuspendingPointerInputModifierNode { highlightOnLongPress() })

    fun update(state: EventHighlightState, event: EventSummaryUi.Normal, key: Any, draggedCard: (@Composable (Modifier) -> Unit)?) {
        this.draggedCard = draggedCard
        if (state == this.state && event == this.event && key == this.key) return
        this.state = state
        this.event = event
        this.key = key
        pointerInput.resetPointerInputHandler()
    }

    private suspend fun PointerInputScope.highlightOnLongPress() = awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture

        val overlay = state.overlayCoordinates?.takeIf { it.isAttached } ?: return@awaitEachGesture
        val area = (findNearestAncestor(EventHighlightAreaNode.TraverseKey) as? EventHighlightAreaNode)
            ?.requireLayoutCoordinates()
            ?: return@awaitEachGesture

        currentValueOf(LocalHapticFeedback).performHapticFeedback(HapticFeedbackType.LongPress)
        val card = requireLayoutCoordinates()
        val pressedAt = overlay.localPositionOf(card, longPress.position)
        state.show(
            EventHighlight(
                event = event,
                key = key,
                startBounds = overlay.localBoundingBoxOf(card, clipBounds = true),
                cardBounds = overlay.localBoundingBoxOf(card, clipBounds = false),
                areaBounds = overlay.localBoundingBoxOf(area, clipBounds = true),
                fingerY = pressedAt.y,
                draggedCard = draggedCard,
            ),
            finger = pressedAt,
        )
        longPress.consume()

        val detachDistance = DetachDistance.toPx()
        var finger = pressedAt
        try {
            do {
                // On the initial pass, ahead of the card's click, which would otherwise open the event on release.
                val event = awaitPointerEvent(PointerEventPass.Initial)
                // Let go by a second finger (see releaseHighlightOnSecondPointer): the gesture is the pinch's now.
                if (!state.isHeld) break
                event.changes.firstOrNull { it.id == longPress.id }?.let { change ->
                    if (card.isAttached && overlay.isAttached) finger = overlay.localPositionOf(card, change.position)
                }
                event.changes.forEach { it.consume() }

                val delta = finger - pressedAt
                val isLifted = event.changes.none { it.pressed }
                when {
                    !state.canDrag -> Unit
                    state.isDragging && isLifted -> state.drop(finger, delta)
                    state.isDragging -> state.dragTo(delta)
                    delta.getDistance() > detachDistance -> {
                        currentValueOf(LocalHapticFeedback).performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                        state.startDrag(delta)
                    }
                    else -> state.pull(delta)
                }
            } while (!isLifted)
        } finally {
            // Already let go of when dropped, or by a second finger.
            state.hide()
        }
    }
}
