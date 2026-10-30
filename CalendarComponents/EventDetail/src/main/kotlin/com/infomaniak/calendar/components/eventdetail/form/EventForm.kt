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

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.component.AttendeesButton
import com.infomaniak.calendar.components.eventdetail.component.CalendarEditable
import com.infomaniak.calendar.components.eventdetail.component.ClassificationStatusSwitch
import com.infomaniak.calendar.components.eventdetail.component.DateAndTimeEditable
import com.infomaniak.calendar.components.eventdetail.component.KMeetButtonSwitch
import com.infomaniak.calendar.components.eventdetail.component.OccupiedStatusSwitch
import com.infomaniak.calendar.components.eventdetail.component.Section
import com.infomaniak.calendar.components.eventdetail.component.TitleEditable
import com.infomaniak.calendar.components.eventdetail.form.component.AllDaySwitch
import com.infomaniak.calendar.components.eventdetail.form.component.recurrence.RecurrenceDialog
import com.infomaniak.calendar.components.eventdetail.form.component.recurrence.RecurrenceItem
import com.infomaniak.calendar.components.eventdetail.form.component.recurrence.recurrenceText
import com.infomaniak.calendar.components.eventdetail.form.component.reminder.AddReminderButton
import com.infomaniak.calendar.components.eventdetail.form.component.reminder.ReminderItem
import com.infomaniak.calendar.components.eventdetail.form.component.reminder.ReminderOffsetDialog
import com.infomaniak.calendar.components.eventdetail.models.EventColorChoice
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.NotificationTime
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi
import com.infomaniak.calendar.components.eventdetail.modifier.EventSharedElement
import com.infomaniak.calendar.components.eventdetail.modifier.ProvideEventSharedTransition
import com.infomaniak.calendar.components.eventdetail.modifier.eventSharedBounds
import com.infomaniak.calendar.components.eventdetail.modifier.eventSharedElement
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendarGroups
import com.infomaniak.calendar.components.eventdetail.state.rememberDateAndTimeState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Reusable component for both the creation and the edition of an event.
 *
 * [sharedTransitionScope] and [animatedVisibilityScope] animate associated components between detail and editing.
 */
@Composable
fun EventForm(
    state: EventFormState,
    onAttendeesClick: () -> Unit,
    onTimeZoneClick: (isStart: Boolean) -> Unit,
    onCustomReminderClick: () -> Unit,
    getRecurrenceText: suspend (RecurrenceUi, start: LocalDateTime) -> String?,
    onCustomRecurrenceClick: (start: LocalDate, recurrence: RecurrenceUi) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) = ProvideEventSharedTransition(sharedTransitionScope, animatedVisibilityScope) {
    Column(modifier.padding(contentPadding)) {
        TitleEditable(
            dotColor = { state.eventColorState.value.getColor(state.calendar) },
            textFieldState = state.titleTextState,
            modifier = Modifier.eventSharedElement(EventSharedElement.Title),
        )

        AllDaySwitch(
            checked = state.isAllDay::value,
            onCheckedChange = { isChecked -> state.isAllDay.value = isChecked },
        )

        DateAndTimeEditable(
            dateAndTimeState = state.dateAndTimeState,
            isAllDay = state.isAllDay::value,
            onTimeZoneClick = onTimeZoneClick,
            modifier = Modifier.eventSharedBounds(EventSharedElement.DateAndTime),
        )

        state.dateAndTimeState?.let { dateAndTimeState ->
            var isRecurrenceDialogVisible by rememberSaveable { mutableStateOf(false) }
            val start = dateAndTimeState.start.date.atTime(dateAndTimeState.start.time)

            RecurrenceItem(
                text = recurrenceText(state.recurrence.value, start, getRecurrenceText),
                onClick = { isRecurrenceDialogVisible = true },
            )

            if (isRecurrenceDialogVisible) {
                RecurrenceDialog(
                    selectedRecurrence = state.recurrence.value,
                    start = start,
                    getRecurrenceText = getRecurrenceText,
                    onRecurrenceSelect = { recurrence ->
                        state.recurrence.value = recurrence
                        isRecurrenceDialogVisible = false
                    },
                    onCustomClick = {
                        isRecurrenceDialogVisible = false
                        onCustomRecurrenceClick(start.date, state.recurrence.value)
                    },
                    onDismissRequest = { isRecurrenceDialogVisible = false },
                )
            }
        }

        Section(EventSharedElement.Divider1) {
            AttendeesButton(
                attendees = state.attendees.toList(),
                onClick = onAttendeesClick,
                modifier = Modifier.eventSharedElement(EventSharedElement.Attendees),
            )

            KMeetButtonSwitch(
                checked = state.isKMeetUrlEnabled::value,
                onCheckedChange = { isChecked -> state.isKMeetUrlEnabled.value = isChecked },
                modifier = Modifier.eventSharedBounds(EventSharedElement.KMeet),
            )
        }

        Section(EventSharedElement.Divider3, Modifier.animateContentSize()) {
            var isReminderOffsetDialogVisible by rememberSaveable { mutableStateOf(false) }

            state.notifications.value.forEach { notification ->
                ReminderItem(
                    notification = notification,
                    onRemoveClick = { state.notifications.value -= notification },
                )
            }

            AddReminderButton(onClick = { isReminderOffsetDialogVisible = true })

            if (isReminderOffsetDialogVisible) {
                ReminderOffsetDialog(
                    selectedOffset = null,
                    onOffsetSelect = { offset ->
                        if (offset != null) {
                            state.notifications.value += EventDetailUi.Notification(
                                type = EventDetailUi.Notification.Type.Push,
                                time = NotificationTime.Offset(offset),
                            )
                        }
                        isReminderOffsetDialogVisible = false
                    },
                    onCustomClick = {
                        isReminderOffsetDialogVisible = false
                        onCustomReminderClick()
                    },
                    onDismissRequest = { isReminderOffsetDialogVisible = false },
                )
            }
        }

        Section(EventSharedElement.Divider4) {
            OccupiedStatusSwitch(
                checked = state.isOccupied::value,
                onCheckedChange = { isChecked -> state.isOccupied.value = isChecked },
                modifier = Modifier.eventSharedBounds(EventSharedElement.OccupiedStatus),
            )

            ClassificationStatusSwitch(
                checked = state.isPublic::value,
                onCheckedChange = { isChecked -> state.isPublic.value = isChecked },
                modifier = Modifier.eventSharedBounds(EventSharedElement.ClassificationStatus),
            )

            CalendarEditable(
                selectedCalendar = state.calendar,
                calendars = state.calendars,
                onSelect = { calendar -> state.calendar = calendar },
                modifier = Modifier.eventSharedBounds(EventSharedElement.Calendar),
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            EventForm(
                state = rememberEventFormState(
                    calendars = previewEventDetailCalendarGroups,
                    initialEventColor = EventColorChoice.Default,
                    initialCalendar = previewEventDetailCalendar,
                    dateAndTimeState = rememberDateAndTimeState(1.hours, TimeZone.currentSystemDefault()),
                    initialText = "Event title",
                    attendees = remember { mutableStateSetOf() },
                    initialIsAllDay = false,
                    initialRecurrence = RecurrenceUi.Rule.presets.first(),
                    initialIsKMeetUrlEnabled = false,
                    initialNotifications = listOf(
                        EventDetailUi.Notification(EventDetailUi.Notification.Type.Push, NotificationTime.Offset((-15).minutes)),
                    ),
                    initialIsOccupied = true,
                    initialIsPublic = true,
                ),
                onAttendeesClick = {},
                onTimeZoneClick = {},
                onCustomReminderClick = {},
                getRecurrenceText = { recurrence, _ -> (recurrence as? RecurrenceUi.Rule)?.frequency?.name },
                onCustomRecurrenceClick = { _, _ -> },
                contentPadding = PaddingValues(16.dp),
            )
        }
    }
}
