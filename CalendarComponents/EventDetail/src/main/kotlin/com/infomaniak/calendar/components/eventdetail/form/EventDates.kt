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
package com.infomaniak.calendar.components.eventdetail.form

import androidx.compose.runtime.Immutable
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.serializers.TimeZoneSerializer
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import java.time.temporal.ChronoUnit
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

@Immutable
@Serializable
data class EventDateTime(
    val date: LocalDate,
    val time: LocalTime,
    @Serializable(with = TimeZoneSerializer::class) val timeZone: TimeZone,
) {
    val dateTime: LocalDateTime get() = date.atTime(time)

    internal val instant: Instant get() = dateTime.toInstant(timeZone)

    internal fun at(instant: Instant): EventDateTime {
        val dateTime = instant.toLocalDateTime(timeZone)
        return copy(date = dateTime.date, time = dateTime.time)
    }
}

/** The start and end of an event. Picking one of them moves the other when needed, keeping the event's duration. */
@Immutable
@Serializable
data class EventDates(
    val start: EventDateTime,
    val end: EventDateTime,
    val hasEndEverBeenTouched: Boolean = false,
) {
    fun pickDate(isStart: Boolean, date: LocalDate): EventDates = editEntry(isStart) { it.copy(date = date) }

    fun pickTime(isStart: Boolean, time: LocalTime): EventDates = editEntry(isStart) { it.copy(time = time) }

    fun pickTimeZone(isStart: Boolean, timeZone: TimeZone): EventDates = if (isStart) {
        val areEntriesSynced = start.timeZone == end.timeZone
        edit(isEditingStart = true) {
            copy(start = start.copy(timeZone = timeZone), end = if (areEntriesSynced) end.copy(timeZone = timeZone) else end)
        }
    } else {
        editEntry(isStart = false) { it.copy(timeZone = timeZone) }
    }

    private fun editEntry(isStart: Boolean, change: (EventDateTime) -> EventDateTime): EventDates = edit(isStart) {
        if (isStart) copy(start = change(start)) else copy(end = change(end))
    }

    /** Single place adjusting the other values after the user picked a new one. */
    private fun edit(isEditingStart: Boolean, change: EventDates.() -> EventDates): EventDates {
        // TODO: This interval displacement doesn't work if the user is setting an all day event and the time component of
        //  both entries are at 0:00 and the range comprises a day of 23 or 25 hours.
        //  Adapt the interval computation to take into account days 23 or 25 hours. Changing the date, should not change the time.
        val previousInterval = end.instant - start.instant
        val changed = change().let { if (isEditingStart) it else it.copy(hasEndEverBeenTouched = true) }
        val isOverlapping = changed.start.instant > changed.end.instant

        return when {
            isEditingStart && (isOverlapping || !changed.hasEndEverBeenTouched) -> {
                changed.copy(end = changed.end.at(changed.start.instant + previousInterval))
            }
            !isEditingStart && isOverlapping -> changed.copy(start = changed.start.at(changed.end.instant - previousInterval))
            else -> changed
        }
    }

    companion object {
        /**
         * The dates of a new event on [date].
         *
         * The start is the next full hour if the date is today, and the end is [eventDuration] after the start.
         * Otherwise, the start is 8:00 AM on the given date, and the end is [eventDuration] after the start.
         */
        fun default(date: LocalDate, timeZone: TimeZone, eventDuration: Duration): EventDates {
            val start = if (date == Clock.today(timeZone)) Clock.System.now().nextWholeHour(timeZone) else date.atTime(8, 0)
            val end = start.toInstant(timeZone).plus(eventDuration).toLocalDateTime(timeZone)

            return EventDates(
                start = EventDateTime(start.date, start.time, timeZone),
                end = EventDateTime(end.date, end.time, timeZone),
            )
        }

        private fun Instant.nextWholeHour(timeZone: TimeZone): LocalDateTime {
            return toLocalDateTime(timeZone).toJavaLocalDateTime().truncatedTo(ChronoUnit.HOURS).plusHours(1).toKotlinLocalDateTime()
        }
    }
}
