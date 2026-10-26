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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateSet
import com.infomaniak.calendar.components.eventdetail.models.EventColorChoice
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.components.eventdetail.state.DateAndTimeState
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import kotlinx.datetime.atTime

/**
 * @param dateAndTimeState Can be null so it can be initialized in two steps after the data has been read from disk.
 */
@Stable
class EventFormState(
    val titleTextState: TextFieldState,
    val eventColorState: MutableState<EventColorChoice>,
    selectedCalendarState: MutableState<EventDetailCalendar>,
    val isAllDay: MutableState<Boolean>,
    val dateAndTimeState: DateAndTimeState?,
    val attendees: SnapshotStateSet<AttendeeUi>,
    val isKMeetUrlEnabled: MutableState<Boolean>,
    val notifications: MutableState<List<EventDetailUi.Notification>>,
    val isOccupied: MutableState<Boolean>,
    val isPublic: MutableState<Boolean>,
    val calendars: List<EventDetailCalendarGroup>,
) {
    var calendar by selectedCalendarState

    fun toEventDraft(): EventDraft? {
        dateAndTimeState ?: return null

        return EventDraft(
            title = titleTextState.text.toString(),
            color = eventColorState.value.getColor(calendar),
            calendarId = calendar.id,
            isAllDay = isAllDay.value,
            start = dateAndTimeState.start.date.atTime(dateAndTimeState.start.time),
            startTimeZone = dateAndTimeState.start.timeZone,
            end = dateAndTimeState.end.date.atTime(dateAndTimeState.end.time),
            endTimeZone = dateAndTimeState.end.timeZone,
            attendees = attendees.toList(),
            isKMeetUrlEnabled = isKMeetUrlEnabled.value,
            notifications = notifications.value,
            isOccupied = isOccupied.value,
            isPublic = isPublic.value,
        )
    }
}

@Composable
fun rememberDefaultEventForm(
    calendars: List<EventDetailCalendarGroup>,
    initialCalendar: EventDetailCalendar,
    dateAndTimeState: DateAndTimeState?,
    attendees: SnapshotStateSet<AttendeeUi>,
): EventFormState = rememberEventFormState(
    calendars = calendars,
    initialEventColor = EventColorChoice.Default,
    initialCalendar = initialCalendar,
    dateAndTimeState = dateAndTimeState,
    initialText = "",
    initialIsAllDay = false,
    initialIsKMeetUrlEnabled = false,
    initialNotifications = emptyList(),
    attendees = attendees,
    initialIsOccupied = true,
    initialIsPublic = true,
)

@Composable
fun rememberEventFormState(
    calendars: List<EventDetailCalendarGroup>,
    initialEventColor: EventColorChoice,
    initialCalendar: EventDetailCalendar,
    dateAndTimeState: DateAndTimeState?,
    initialText: String,
    initialIsAllDay: Boolean,
    initialIsKMeetUrlEnabled: Boolean,
    initialNotifications: List<EventDetailUi.Notification>,
    attendees: SnapshotStateSet<AttendeeUi>,
    initialIsOccupied: Boolean,
    initialIsPublic: Boolean,
): EventFormState = EventFormState(
    titleTextState = rememberTextFieldState(initialText),
    eventColorState = rememberSaveable { mutableStateOf(initialEventColor) },
    selectedCalendarState = rememberSaveable { mutableStateOf(initialCalendar) },
    isAllDay = rememberSaveable { mutableStateOf(initialIsAllDay) },
    dateAndTimeState = dateAndTimeState,
    attendees = attendees,
    isKMeetUrlEnabled = rememberSaveable { mutableStateOf(initialIsKMeetUrlEnabled) },
    notifications = rememberSaveable { mutableStateOf(initialNotifications) },
    isOccupied = rememberSaveable { mutableStateOf(initialIsOccupied) },
    isPublic = rememberSaveable { mutableStateOf(initialIsPublic) },
    calendars = calendars,
)
