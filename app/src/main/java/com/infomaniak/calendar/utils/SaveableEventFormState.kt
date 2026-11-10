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
package com.infomaniak.calendar.utils

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.form.EventFormValues
import com.infomaniak.calendar.components.eventdetail.state.DateAndTimeState
import com.infomaniak.calendar.components.eventdetail.state.TimeZoneSaver
import com.infomaniak.calendar.components.foundation.models.OrganizerSource
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState

/** An [EventFormState] starting from [values], whose fields are restored from this handle when it holds them. */
@OptIn(SavedStateHandleSaveableApi::class)
fun SavedStateHandle.saveableEventFormState(
    values: EventFormValues,
    attendeesSearchState: AttendeesSearchState<OrganizerSource>,
): EventFormState = EventFormState(
    titleTextState = saveable("title", saver = TextFieldState.Saver) { TextFieldState(values.title) },
    eventColorState = saveableState("eventColor", values.eventColor),
    selectedCalendarState = saveableState("calendar", values.calendar),
    isAllDay = saveableState("isAllDay", values.isAllDay),
    dateAndTimeState = DateAndTimeState(
        start = DateAndTimeState.Entry(
            date = saveableState("startDate", values.start.date),
            time = saveableState("startTime", values.start.time),
            timeZone = saveable("startTimeZone", stateSaver = TimeZoneSaver) { mutableStateOf(values.startTimeZone) },
        ),
        end = DateAndTimeState.Entry(
            date = saveableState("endDate", values.end.date),
            time = saveableState("endTime", values.end.time),
            timeZone = saveable("endTimeZone", stateSaver = TimeZoneSaver) { mutableStateOf(values.endTimeZone) },
        ),
        hasEndEverBeenTouched = saveableState("hasEndEverBeenTouched", false),
    ),
    attendees = { attendeesSearchState.attendees },
    initialOrganizer = (attendeesSearchState.organizerSource as? OrganizerSource.Static)?.organizer,
    isKMeetUrlEnabled = saveableState("isKMeetUrlEnabled", values.isKMeetUrlEnabled),
    notifications = saveableState("notifications", values.notifications),
    isOccupied = saveableState("isOccupied", values.isOccupied),
    isPublic = saveableState("isPublic", values.isPublic),
    calendars = values.calendars,
)

@OptIn(SavedStateHandleSaveableApi::class)
private fun <T> SavedStateHandle.saveableState(key: String, initialValue: T): MutableState<T> {
    return saveable(key) { mutableStateOf(initialValue) }
}
