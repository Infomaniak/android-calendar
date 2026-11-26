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
package com.infomaniak.calendar.components.days.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** Where an event held up by a long press was dragged and dropped. */
@Immutable
sealed interface EventMove {

    /** Dropped on a day's hour grid, to start at [start], the quarter of an hour nearest to the card's top. */
    data class Timed(val start: LocalDateTime) : EventMove

    /** Dropped among the all-day events, to start on [startDate], the day under the finger. */
    data class AllDay(val startDate: LocalDate) : EventMove
}
