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
package com.infomaniak.calendar.components.day.component

import androidx.compose.ui.Modifier
import com.infomaniak.calendar.components.day.model.MINUTES_PER_DAY
import com.infomaniak.calendar.components.day.model.MINUTES_PER_HOUR
import com.infomaniak.calendar.components.event.highlight.EventHighlightState
import com.infomaniak.calendar.components.event.highlight.eventDropTarget
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.math.roundToInt

private const val DROP_STEP_MINUTES = 15

/**
 * Marks a day's timeline, running from midnight to midnight, as somewhere [state]'s event can be dropped on
 * [date], at the quarter of an hour nearest to the card's top, leaving the card room before midnight.
 */
internal fun Modifier.timelineDropTarget(state: EventHighlightState, date: LocalDate): Modifier {
    return eventDropTarget(state, date, timeAt = ::quarterHourAt)
}

private fun quarterHourAt(topFraction: Float, heightFraction: Float): LocalTime {
    val duration = (heightFraction * MINUTES_PER_DAY).roundToInt().coerceIn(DROP_STEP_MINUTES, MINUTES_PER_DAY)
    val latestStart = (MINUTES_PER_DAY - duration) / DROP_STEP_MINUTES * DROP_STEP_MINUTES
    val minute = ((topFraction * MINUTES_PER_DAY / DROP_STEP_MINUTES).roundToInt() * DROP_STEP_MINUTES)
        .coerceIn(0, latestStart)
    return LocalTime(hour = minute / MINUTES_PER_HOUR, minute = minute % MINUTES_PER_HOUR)
}
