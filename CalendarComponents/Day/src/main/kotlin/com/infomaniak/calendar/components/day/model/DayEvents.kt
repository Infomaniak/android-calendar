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
package com.infomaniak.calendar.components.day.model

import androidx.compose.runtime.Immutable
import com.infomaniak.calendar.components.foundation.models.EventUi
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

const val HOURS_PER_DAY: Int = 24
const val MINUTES_PER_HOUR: Int = 60
const val MINUTES_PER_DAY: Int = HOURS_PER_DAY * MINUTES_PER_HOUR

@Immutable
data class DayEvents(
    val allDay: List<EventUi.Normal>,
    val timed: List<TimedEvent>,
) {
    companion object {
        val Empty = DayEvents(allDay = emptyList(), timed = emptyList())
    }
}

@Immutable
data class TimedEvent(
    val event: EventUi.Normal,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
) {
    val durationMinutes: Int get() = endMinuteOfDay - startMinuteOfDay
}

fun EventUi.Normal.toTimedEvent(date: LocalDate, timeZone: TimeZone): TimedEvent = TimedEvent(
    event = this,
    startMinuteOfDay = start.minuteOfDayWithin(date, timeZone),
    endMinuteOfDay = end.minuteOfDayWithin(date, timeZone),
)

/**
 * Where the instant reads on [date]'s clock, or the edge of the day it falls beyond, for the days
 * of a multi-day event that only hold one of its two ends.
 *
 * The grid draws a plain 24-hour clock, so this has to be the time the event shows on that clock,
 * not the time elapsed since midnight: the two differ by an hour for the rest of a day the clocks
 * changed on, which would draw every event after the change one hour off the line naming its time.
 */
private fun Instant.minuteOfDayWithin(date: LocalDate, timeZone: TimeZone): Int {
    val localDateTime = toLocalDateTime(timeZone)

    return when {
        localDateTime.date < date -> 0
        localDateTime.date > date -> MINUTES_PER_DAY
        else -> localDateTime.hour * MINUTES_PER_HOUR + localDateTime.minute
    }
}
