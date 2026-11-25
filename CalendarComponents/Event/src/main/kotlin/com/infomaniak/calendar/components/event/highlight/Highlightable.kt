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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi

private const val DRAGGED_TRACE_ALPHA = 0.35f

/**
 * Lets a long press hold [event] up in [state], and hides this card while [HighlightedEventOverlay] draws it in its place.
 * Dragged elsewhere, only a trace of it is left here.
 *
 * [key] tells this card apart from the others showing the same event, like the slices of an event over several days.
 * [draggedCard] draws the card while it is dragged, at its own size: the Planning's event item when not given.
 */
fun Modifier.highlightable(
    state: EventHighlightState,
    event: EventSummaryUi.Normal,
    key: Any = event,
    draggedCard: (@Composable (Modifier) -> Unit)? = null,
): Modifier = this
    .highlightOnLongPress(state, event, key, draggedCard)
    .graphicsLayer {
        alpha = when {
            !state.isHighlighting(key) -> 1f
            state.isDragging -> DRAGGED_TRACE_ALPHA
            else -> 0f
        }
    }
