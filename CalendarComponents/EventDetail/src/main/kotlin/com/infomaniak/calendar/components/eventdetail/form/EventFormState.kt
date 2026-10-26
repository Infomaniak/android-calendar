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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.components.eventdetail.state.DateAndTimeState
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import kotlinx.datetime.atTime

/**
 * @param dateAndTimeState Can be null so it can be initialized in two steps after the data has been read from disk.
 */
@Stable
class EventFormState(
    val titleTextState: TextFieldState,
    val eventColorState: MutableState<Color?>,
    selectedCalendarState: MutableState<EventDetailCalendar>,
    val isAllDay: MutableState<Boolean>,
    val dateAndTimeState: DateAndTimeState?,
    val attendeesState: AttendeesSearchState,
    val isKMeetUrlEnabled: MutableState<Boolean>,
    val isOccupied: MutableState<Boolean>,
    val isPublic: MutableState<Boolean>,
    val calendars: List<EventDetailCalendarGroup>,
) {
    var calendar by selectedCalendarState

    fun toEventDraft(): EventDraft? {
        dateAndTimeState ?: return null

        return EventDraft(
            title = titleTextState.text.toString(),
            color = eventColorState.value ?: return null,
            calendarId = calendar.id,
            isAllDay = isAllDay.value,
            start = dateAndTimeState.start.date.atTime(dateAndTimeState.start.time),
            startTimeZone = dateAndTimeState.start.timeZone,
            end = dateAndTimeState.end.date.atTime(dateAndTimeState.end.time),
            endTimeZone = dateAndTimeState.end.timeZone,
            attendees = attendeesState.attendees.toList(),
            isKMeetUrlEnabled = isKMeetUrlEnabled.value,
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
    attendeesSearchState: AttendeesSearchState,
): EventFormState = rememberEventFormState(
    calendars = calendars,
    initialEventColor = initialCalendar.color,
    initialCalendar = initialCalendar,
    dateAndTimeState = dateAndTimeState,
    initialText = "",
    initialIsAllDay = false,
    initialIsKMeetUrlEnabled = false,
    attendeesSearchState = attendeesSearchState,
    initialIsOccupied = true,
    initialIsPublic = true,
)

@Composable
fun rememberEventFormState(
    calendars: List<EventDetailCalendarGroup>,
    initialEventColor: Color,
    initialCalendar: EventDetailCalendar,
    dateAndTimeState: DateAndTimeState?,
    initialText: String,
    initialIsAllDay: Boolean,
    initialIsKMeetUrlEnabled: Boolean,
    attendeesSearchState: AttendeesSearchState,
    initialIsOccupied: Boolean,
    initialIsPublic: Boolean,
): EventFormState {
    val eventColorState = rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(initialEventColor) }

    return EventFormState(
        titleTextState = rememberTextFieldState(initialText),
        eventColorState = eventColorState,
        selectedCalendarState = rememberSaveable { mutableStateOf(initialCalendar) },
        isAllDay = rememberSaveable { mutableStateOf(initialIsAllDay) },
        dateAndTimeState = dateAndTimeState,
        attendeesState = attendeesSearchState,
        isKMeetUrlEnabled = rememberSaveable { mutableStateOf(initialIsKMeetUrlEnabled) },
        isOccupied = rememberSaveable { mutableStateOf(initialIsOccupied) },
        isPublic = rememberSaveable { mutableStateOf(initialIsPublic) },
        calendars = calendars,
    )
}

private val ColorSaver = Saver<Color?, String>(
    save = { color -> color?.value?.toString() ?: "null" },
    restore = { stringValue -> if (stringValue == "null") null else Color(stringValue.toULong()) },
)
