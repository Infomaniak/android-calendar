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
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi

/**
 * An event held up by a long press on the card [key] identifies. The card grows from where it sat, [startBounds], to
 * the whole width of [areaBounds], opening near the finger, [fingerY]. All of them are in the coordinates of the
 * overlay drawing it. Dragged, it is drawn by [draggedCard], the way it looks where it sat, in [draggedBounds] while
 * the finger is where it pressed, then back in [startBounds] once dropped.
 */
@Immutable
internal data class EventHighlight(
    val event: EventSummaryUi.Normal,
    val key: Any,
    val startBounds: Rect,
    val areaBounds: Rect,
    val fingerY: Float,
    val draggedCard: (@Composable (Modifier) -> Unit)? = null,
    val draggedBounds: Rect = startBounds,
)
