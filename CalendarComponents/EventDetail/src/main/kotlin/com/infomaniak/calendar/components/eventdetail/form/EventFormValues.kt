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
import com.infomaniak.calendar.components.eventdetail.state.computeDefaultValues
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.OrganizerSource
import com.infomaniak.calendar.components.foundation.models.ParticipantUi
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.serializers.TimeZoneSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/** What the user filled in an event form, as saved and sent. */
@Immutable
@Serializable
data class EventFormValues(
    val title: String,
    val calendarId: String,
    val eventColor: EventColorChoice,
    val isAllDay: Boolean,
    val start: LocalDateTime,
    @Serializable(with = TimeZoneSerializer::class) val startTimeZone: TimeZone,
    val end: LocalDateTime,
    @Serializable(with = TimeZoneSerializer::class) val endTimeZone: TimeZone,
    val hasEndEverBeenTouched: Boolean,
    val participants: Set<ParticipantUi>,
    /** [OrganizerSource.Dynamic] for a new event, which follows the selected calendar. */
    val organizerSource: OrganizerSource?,
    val isKMeetUrlEnabled: Boolean,
    val notifications: List<EventDetailUi.Notification>,
    val isOccupied: Boolean,
    val isPublic: Boolean,
) {
    val attendees: List<AttendeeUi>
        get() {
            val me = (organizerSource as? OrganizerSource.Dynamic)?.owner
            return participants.mapNotNull { it.toAttendeeUi(me) }
        }

    fun toEventDraft(calendar: EventDetailCalendar) = EventDraft(
        title = title,
        color = eventColor.getColor(calendar),
        calendarId = calendarId,
        isAllDay = isAllDay,
        start = start,
        startTimeZone = startTimeZone,
        end = end,
        endTimeZone = endTimeZone,
        attendees = attendees,
        initialOrganizer = (organizerSource as? OrganizerSource.Static)?.organizer,
        isKMeetUrlEnabled = isKMeetUrlEnabled,
        notifications = notifications,
        isOccupied = isOccupied,
        isPublic = isPublic,
    )

    companion object {
        /** A new event on [date] in [calendar], lasting [eventDuration]. */
        fun default(calendar: EventDetailCalendar, date: LocalDate, timeZone: TimeZone, eventDuration: Duration): EventFormValues {
            val (start, end) = computeDefaultValues(date, timeZone, eventDuration)

            return EventFormValues(
                title = "",
                calendarId = calendar.id,
                eventColor = EventColorChoice.Default,
                isAllDay = false,
                start = start,
                startTimeZone = timeZone,
                end = end,
                endTimeZone = timeZone,
                hasEndEverBeenTouched = false,
                participants = emptySet(),
                organizerSource = calendar.organizerAttendeeUi?.let(OrganizerSource::Dynamic),
                isKMeetUrlEnabled = false,
                notifications = emptyList(),
                isOccupied = true,
                isPublic = true,
            )
        }
    }
}

fun List<EventDetailCalendarGroup>.calendarOf(id: String): EventDetailCalendar? {
    return firstNotNullOfOrNull { group -> group.calendars.find { it.id == id } }
}
