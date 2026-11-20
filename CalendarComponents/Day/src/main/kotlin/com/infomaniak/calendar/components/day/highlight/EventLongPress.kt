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
package com.infomaniak.calendar.components.day.highlight

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.requireLayoutCoordinates
import androidx.compose.ui.node.findNearestAncestor
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.day.model.TimedEvent

/** How far the finger pulls the unfolded card before it comes loose and can be dropped elsewhere. */
private val DetachDistance = 40.dp

/**
 * Holds [timedEvent] up in [state] for as long as a long press on its card lasts.
 *
 * Until the long press is recognised, the gesture belongs to whoever claims it: a tap still opens the
 * event, and a scroll, a swipe or a pinch that starts first cancels the long press. Once recognised,
 * the gesture is the highlight's own: every change is consumed, ahead of the card's click and the
 * timeline's scrolling, until the finger lifts or [releaseHighlightOnSecondPointer] lets the event go.
 *
 * Moving the finger pulls the card along, until [DetachDistance] away it comes loose: lifted then, it
 * is dropped where it is (see [EventHighlightState.drop]). Its texts are cut by [textOverflow], as in its column.
 */
internal fun Modifier.highlightOnLongPress(
    state: EventHighlightState,
    timedEvent: TimedEvent,
    textOverflow: TextOverflow,
): Modifier {
    return this then LongPressHighlightElement(state, timedEvent, textOverflow)
}

private data class LongPressHighlightElement(
    val state: EventHighlightState,
    val timedEvent: TimedEvent,
    val textOverflow: TextOverflow,
) : ModifierNodeElement<LongPressHighlightNode>() {
    override fun create() = LongPressHighlightNode(state, timedEvent, textOverflow)
    override fun update(node: LongPressHighlightNode) = node.update(state, timedEvent, textOverflow)
}

private class LongPressHighlightNode(
    private var state: EventHighlightState,
    private var timedEvent: TimedEvent,
    private var textOverflow: TextOverflow,
) : DelegatingNode(), CompositionLocalConsumerModifierNode {

    private val pointerInput = delegate(SuspendingPointerInputModifierNode { highlightOnLongPress() })

    fun update(state: EventHighlightState, timedEvent: TimedEvent, textOverflow: TextOverflow) {
        this.textOverflow = textOverflow
        if (state == this.state && timedEvent == this.timedEvent) return
        this.state = state
        this.timedEvent = timedEvent
        pointerInput.resetPointerInputHandler()
    }

    private suspend fun PointerInputScope.highlightOnLongPress() = awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture

        val overlay = state.overlayCoordinates?.takeIf { it.isAttached } ?: return@awaitEachGesture
        val area = (findNearestAncestor(EventHighlightAreaNode.TraverseKey) as? EventHighlightAreaNode)
            ?.requireLayoutCoordinates()
            ?: return@awaitEachGesture

        val haptics = currentValueOf(LocalHapticFeedback)
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        val card = requireLayoutCoordinates()
        state.show(
            EventHighlight(
                timedEvent = timedEvent,
                startBounds = overlay.localBoundingBoxOf(card, clipBounds = true),
                areaBounds = overlay.localBoundingBoxOf(area, clipBounds = true),
                fingerY = overlay.localPositionOf(card, longPress.position).y,
                textOverflow = textOverflow,
            ),
        )
        longPress.consume()

        val detachDistance = DetachDistance.toPx()
        var isDropped = false

        try {
            do {
                // On the initial pass, ahead of the card's click, which would otherwise open the event on release.
                val event = awaitPointerEvent(PointerEventPass.Initial)
                // Let go by a second finger (see releaseHighlightOnSecondPointer): the gesture is the pinch's now.
                if (!state.isHeld) break

                event.changes.firstOrNull { it.id == longPress.id }?.let { change ->
                    val delta = change.position - longPress.position
                    when {
                        state.isDragging -> state.dragTo(delta)
                        delta.getDistance() > detachDistance -> {
                            haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                            state.startDrag(delta)
                        }
                        else -> state.pull(delta)
                    }
                    if (!change.pressed && state.isDragging) {
                        state.drop(fingerX = overlay.localPositionOf(card, change.position).x, delta = delta)
                        isDropped = true
                    }
                }
                event.changes.forEach { it.consume() }
            } while (event.changes.any { it.pressed })
        } finally {
            if (!isDropped) state.hide()
        }
    }
}
