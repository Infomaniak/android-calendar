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
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi

/**
 * Holds [event] up in [state] for as long as a long press on its card, which [key] identifies, lasts.
 *
 * Until the long press is recognised, the gesture belongs to whoever claims it: a tap still opens the
 * event, and a scroll, a swipe or a pinch that starts first cancels the long press. Once recognised,
 * the gesture is the highlight's own: every change is consumed, ahead of the card's click and the
 * timeline's scrolling, until the finger lifts or [releaseHighlightOnSecondPointer] lets the event go.
 */
internal fun Modifier.highlightOnLongPress(state: EventHighlightState, event: EventSummaryUi.Normal, key: Any): Modifier {
    return this then LongPressHighlightElement(state, event, key)
}

private data class LongPressHighlightElement(
    val state: EventHighlightState,
    val event: EventSummaryUi.Normal,
    val key: Any,
) : ModifierNodeElement<LongPressHighlightNode>() {
    override fun create() = LongPressHighlightNode(state, event, key)
    override fun update(node: LongPressHighlightNode) = node.update(state, event, key)
}

private class LongPressHighlightNode(
    private var state: EventHighlightState,
    private var event: EventSummaryUi.Normal,
    private var key: Any,
) : DelegatingNode(), CompositionLocalConsumerModifierNode {

    private val pointerInput = delegate(SuspendingPointerInputModifierNode { highlightOnLongPress() })

    fun update(state: EventHighlightState, event: EventSummaryUi.Normal, key: Any) {
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
        state.show(
            EventHighlight(
                event = event,
                key = key,
                startBounds = overlay.localBoundingBoxOf(card, clipBounds = true),
                areaBounds = overlay.localBoundingBoxOf(area, clipBounds = true),
                fingerY = overlay.localPositionOf(card, longPress.position).y,
            ),
        )
        longPress.consume()

        try {
            do {
                // On the initial pass, ahead of the card's click, which would otherwise open the event on release.
                val event = awaitPointerEvent(PointerEventPass.Initial)
                // Let go by a second finger (see releaseHighlightOnSecondPointer): the gesture is the pinch's now.
                if (!state.isHeld) break
                event.changes.forEach { it.consume() }
            } while (event.changes.any { it.pressed })
        } finally {
            state.hide()
        }
    }
}
