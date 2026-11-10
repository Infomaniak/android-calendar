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
import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.OrganizerUi
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/** What the user filled in an event form. */
@Immutable
@Serializable
data class EventFormValues(
    val title: String,
    val calendarId: String,
    val eventColor: EventColorChoice,
    val isAllDay: Boolean,
    val dates: EventDates,
    val isKMeetUrlEnabled: Boolean,
    val notifications: List<EventDetailUi.Notification>,
    val isOccupied: Boolean,
    val isPublic: Boolean,
) {
    fun toEventDraft(calendar: EventDetailCalendar, attendees: List<AttendeeUi>, initialOrganizer: OrganizerUi?) = EventDraft(
        title = title,
        color = eventColor.getColor(calendar),
        calendarId = calendarId,
        isAllDay = isAllDay,
        start = dates.start.dateTime,
        startTimeZone = dates.start.timeZone,
        end = dates.end.dateTime,
        endTimeZone = dates.end.timeZone,
        attendees = attendees,
        initialOrganizer = initialOrganizer,
        isKMeetUrlEnabled = isKMeetUrlEnabled,
        notifications = notifications,
        isOccupied = isOccupied,
        isPublic = isPublic,
    )

    companion object {
        /** A new event on [date] in [calendarId], lasting [eventDuration]. */
        fun default(calendarId: String, date: LocalDate, timeZone: TimeZone, eventDuration: Duration) = EventFormValues(
            title = "",
            calendarId = calendarId,
            eventColor = EventColorChoice.Default,
            isAllDay = false,
            dates = EventDates.default(date, timeZone, eventDuration),
            isKMeetUrlEnabled = false,
            notifications = emptyList(),
            isOccupied = true,
            isPublic = true,
        )
    }
}

fun List<EventDetailCalendarGroup>.calendarOf(id: String): EventDetailCalendar? {
    return firstNotNullOfOrNull { group -> group.calendars.find { it.id == id } }
}
