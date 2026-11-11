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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.OrganizerSource
import com.infomaniak.calendar.components.foundation.models.ParticipantUi

/**
 * The editable state of an event form, one state per field, starting from [values] and read back with [toValues].
 *
 * @param calendars The calendars the event can be put in, kept up to date by its owner.
 */
@Stable
class EventFormState(values: EventFormValues, calendars: List<EventDetailCalendarGroup>) {
    val titleTextState = TextFieldState(values.title)
    val eventColorState = mutableStateOf(values.eventColor)
    var calendarId by mutableStateOf(values.calendarId)
        private set
    val isAllDay = mutableStateOf(values.isAllDay)
    var dates by mutableStateOf(values.dates)
    val participants = mutableStateSetOf<ParticipantUi>().apply { addAll(values.participants) }
    /** [OrganizerSource.Dynamic] for a new event, which follows the selected calendar. */
    var organizerSource by mutableStateOf(values.organizerSource)
        private set
    val isKMeetUrlEnabled = mutableStateOf(values.isKMeetUrlEnabled)
    val notifications = mutableStateOf(values.notifications)
    val isOccupied = mutableStateOf(values.isOccupied)
    val isPublic = mutableStateOf(values.isPublic)
    var calendars by mutableStateOf(calendars)

    val calendar: EventDetailCalendar? get() = calendars.calendarOf(calendarId)

    /** `false` while the calendars aren't known, and once the selected one can't be written to anymore. */
    val canSubmit: Boolean get() = calendar != null

    val attendees: List<AttendeeUi> by derivedStateOf {
        val me = (organizerSource as? OrganizerSource.Dynamic)?.owner
        participants.mapNotNull { it.toAttendeeUi(me) }
    }

    /** Organizing a new event as the owner of [calendar], who must then not also be one of its attendees. */
    fun selectCalendar(calendar: EventDetailCalendar) {
        calendarId = calendar.id
        val owner = calendar.organizerAttendeeUi
        if (organizerSource is OrganizerSource.Static || owner == null) return

        participants.removeAll { it is AttendeeUi && it.email == owner.email }
        organizerSource = OrganizerSource.Dynamic(owner)
    }

    fun toEventDraft(): EventDraft? = calendar?.let(toValues()::toEventDraft)

    fun toValues() = EventFormValues(
        title = titleTextState.text.toString(),
        calendarId = calendarId,
        eventColor = eventColorState.value,
        isAllDay = isAllDay.value,
        dates = dates,
        participants = participants.toSet(),
        organizerSource = organizerSource,
        isKMeetUrlEnabled = isKMeetUrlEnabled.value,
        notifications = notifications.value,
        isOccupied = isOccupied.value,
        isPublic = isPublic.value,
    )
}
