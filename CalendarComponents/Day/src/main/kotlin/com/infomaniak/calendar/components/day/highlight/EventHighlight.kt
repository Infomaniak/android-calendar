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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import com.infomaniak.calendar.components.day.model.TimedEvent

/**
 * An event held up by a long press. Its card grows from where it sat in its column, [startBounds], to the whole width
 * of [areaBounds], opening near the finger, [fingerY]. All of them are in the coordinates of the overlay drawing it.
 */
@Immutable
internal data class EventHighlight(
    val timedEvent: TimedEvent,
    val startBounds: Rect,
    val areaBounds: Rect,
    val fingerY: Float,
)
