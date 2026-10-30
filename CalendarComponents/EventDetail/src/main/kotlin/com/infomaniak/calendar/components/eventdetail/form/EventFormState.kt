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

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import com.infomaniak.calendar.components.eventdetail.models.EventColorChoice
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi
import com.infomaniak.calendar.components.eventdetail.state.DateAndTimeState
import com.infomaniak.calendar.components.eventdetail.state.computeDefaultValues
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.LocationUi
import com.infomaniak.calendar.components.foundation.models.OrganizerSource
import com.infomaniak.calendar.components.foundation.models.ParticipantUi
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlin.time.Duration

/**
 * The editable state of an event form, one state per field.
 *
 * @param organizerSource [OrganizerSource.Dynamic] for a new event, which follows the selected calendar.
 * @param calendars The calendars the event can be put in.
 */
@Stable
class EventFormState(
    val titleTextState: TextFieldState,
    calendar: EventDetailCalendar,
    eventColor: EventColorChoice,
    isAllDay: Boolean,
    start: LocalDateTime,
    startTimeZone: TimeZone,
    end: LocalDateTime,
    endTimeZone: TimeZone,
    hasEndEverBeenTouched: Boolean,
    location: LocationUi?,
    recurrence: RecurrenceUi,
    participants: Set<ParticipantUi>,
    organizerSource: OrganizerSource?,
    isKMeetUrlEnabled: Boolean,
    notifications: List<EventDetailUi.Notification>,
    isOccupied: Boolean,
    isPublic: Boolean,
    val calendars: List<EventDetailCalendarGroup>,
) {
    val eventColorState = mutableStateOf(eventColor)
    var calendar by mutableStateOf(calendar)
        private set
    val isAllDay = mutableStateOf(isAllDay)
    val dateAndTimeState = DateAndTimeState(
        start = DateAndTimeState.Entry(
            date = mutableStateOf(start.date),
            time = mutableStateOf(start.time),
            timeZone = mutableStateOf(startTimeZone),
        ),
        end = DateAndTimeState.Entry(
            date = mutableStateOf(end.date),
            time = mutableStateOf(end.time),
            timeZone = mutableStateOf(endTimeZone),
        ),
        hasEndEverBeenTouched = mutableStateOf(hasEndEverBeenTouched),
    )
    var location by mutableStateOf(location)
    val recurrence = mutableStateOf(recurrence)
    val participants = mutableStateSetOf<ParticipantUi>().apply { addAll(participants) }
    var organizerSource by mutableStateOf(organizerSource)
        private set
    val isKMeetUrlEnabled = mutableStateOf(isKMeetUrlEnabled)
    val notifications = mutableStateListOf<EventDetailUi.Notification>().apply { addAll(notifications) }
    val isOccupied = mutableStateOf(isOccupied)
    val isPublic = mutableStateOf(isPublic)

    val attendees: List<AttendeeUi> by derivedStateOf {
        val me = (this.organizerSource as? OrganizerSource.Dynamic)?.owner
        this.participants.mapNotNull { it.toAttendeeUi(me) }
    }

    internal val start get() = dateAndTimeState.start.date.atTime(dateAndTimeState.start.time)
    internal val end get() = dateAndTimeState.end.date.atTime(dateAndTimeState.end.time)

    /** Organizing a new event as the owner of [calendar], who must then not also be one of its attendees. */
    fun selectCalendar(calendar: EventDetailCalendar) {
        this.calendar = calendar
        val owner = calendar.organizerAttendeeUi
        if (organizerSource is OrganizerSource.Static || owner == null) return

        participants.removeAll { it is AttendeeUi && it.email == owner.email }
        organizerSource = OrganizerSource.Dynamic(owner)
    }

    fun toEventDraft(): EventDraft {
        return EventDraft(
            title = titleTextState.text.toString(),
            color = eventColorState.value.getColor(calendar),
            calendarId = calendar.id,
            isAllDay = isAllDay.value,
            start = start,
            startTimeZone = dateAndTimeState.start.timeZone,
            end = end,
            endTimeZone = dateAndTimeState.end.timeZone,
            location = location,
            recurrence = recurrence.value,
            attendees = attendees,
            initialOrganizer = (organizerSource as? OrganizerSource.Static)?.organizer,
            isKMeetUrlEnabled = isKMeetUrlEnabled.value,
            notifications = notifications.toList(),
            isOccupied = isOccupied.value,
            isPublic = isPublic.value,
        )
    }

    companion object {
        /** A new event on [date] in [calendar], lasting [eventDuration]. */
        fun default(
            calendar: EventDetailCalendar,
            date: LocalDate,
            timeZone: TimeZone,
            eventDuration: Duration,
            calendars: List<EventDetailCalendarGroup>,
        ): EventFormState {
            val (start, end) = computeDefaultValues(date, timeZone, eventDuration)

            return EventFormState(
                titleTextState = TextFieldState(),
                calendar = calendar,
                eventColor = EventColorChoice.Default,
                isAllDay = false,
                start = start,
                startTimeZone = timeZone,
                end = end,
                endTimeZone = timeZone,
                hasEndEverBeenTouched = false,
                location = null,
                recurrence = RecurrenceUi.None,
                participants = emptySet(),
                organizerSource = calendar.organizerAttendeeUi?.let(OrganizerSource::Dynamic),
                isKMeetUrlEnabled = false,
                notifications = emptyList(),
                isOccupied = true,
                isPublic = true,
                calendars = calendars,
            )
        }
    }
}
