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
import com.infomaniak.calendar.components.eventdetail.models.EventColorChoice
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.state.defaultEventStartAndEnd
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.time.Duration

/** The values an [EventFormState] starts from. */
@Immutable
data class EventFormValues(
    val calendars: List<EventDetailCalendarGroup>,
    val calendar: EventDetailCalendar,
    val eventColor: EventColorChoice,
    val title: String,
    val isAllDay: Boolean,
    val start: LocalDateTime,
    val startTimeZone: TimeZone,
    val end: LocalDateTime,
    val endTimeZone: TimeZone,
    val isKMeetUrlEnabled: Boolean,
    val notifications: List<EventDetailUi.Notification>,
    val isOccupied: Boolean,
    val isPublic: Boolean,
) {
    companion object {
        /** A new event on [date], lasting [eventDuration]. */
        fun default(
            calendars: List<EventDetailCalendarGroup>,
            calendar: EventDetailCalendar,
            date: LocalDate,
            timeZone: TimeZone,
            eventDuration: Duration,
        ): EventFormValues {
            val (start, end) = defaultEventStartAndEnd(date, timeZone, eventDuration)

            return EventFormValues(
                calendars = calendars,
                calendar = calendar,
                eventColor = EventColorChoice.Default,
                title = "",
                isAllDay = false,
                start = start,
                startTimeZone = timeZone,
                end = end,
                endTimeZone = timeZone,
                isKMeetUrlEnabled = false,
                notifications = emptyList(),
                isOccupied = true,
                isPublic = true,
            )
        }
    }
}
