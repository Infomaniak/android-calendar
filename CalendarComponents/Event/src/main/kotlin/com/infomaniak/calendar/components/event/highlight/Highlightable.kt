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

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi

/**
 * Lets a long press hold [event] up in [state], and hides this card while [HighlightedEventOverlay] draws it in its place.
 *
 * [key] tells this card apart from the others showing the same event, like the slices of an event over several days.
 */
fun Modifier.highlightable(state: EventHighlightState, event: EventSummaryUi.Normal, key: Any = event): Modifier = this
    .highlightOnLongPress(state, event, key)
    .graphicsLayer { alpha = if (state.isHighlighting(key)) 0f else 1f }
