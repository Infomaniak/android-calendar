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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
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
import com.infomaniak.calendar.components.eventdetail.form.component.reminder.AddReminderButton
import com.infomaniak.calendar.components.eventdetail.form.component.reminder.ReminderItem
import com.infomaniak.calendar.components.eventdetail.form.component.reminder.ReminderOffsetDialog
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.NotificationTime
import com.infomaniak.calendar.components.eventdetail.modifier.EventSharedElement
import com.infomaniak.calendar.components.eventdetail.modifier.ProvideEventSharedTransition
import com.infomaniak.calendar.components.eventdetail.modifier.eventSharedBounds
import com.infomaniak.calendar.components.eventdetail.modifier.eventSharedElement
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendarGroups
import com.infomaniak.calendar.components.foundation.utils.rememberInitialFocusRequester
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
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
    calendars: List<EventDetailCalendarGroup>,
    onAttendeesClick: () -> Unit,
    onTimeZoneClick: (isStart: Boolean) -> Unit,
    onCustomReminderClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) = ProvideEventSharedTransition(sharedTransitionScope, animatedVisibilityScope) {
    val form = state.form
    val calendar = calendars.calendarOf(form.calendarId) ?: return@ProvideEventSharedTransition

    val titleState = rememberTextFieldState(form.title)
    LaunchedEffect(state, titleState) {
        snapshotFlow { titleState.text.toString() }.collect { title -> state.edit { it.copy(title = title) } }
    }

    Column(modifier.padding(contentPadding)) {
        val focusRequester = rememberInitialFocusRequester(shouldRequestFocus = { titleState.text.isEmpty() })
        TitleEditable(
            dotColor = { form.eventColor.getColor(calendar) },
            textFieldState = titleState,
            modifier = Modifier
                .focusRequester(focusRequester)
                .eventSharedElement(EventSharedElement.Title),
        )

        AllDaySwitch(
            checked = { form.isAllDay },
            onCheckedChange = { isAllDay -> state.edit { it.copy(isAllDay = isAllDay) } },
        )

        DateAndTimeEditable(
            dates = form.dates,
            isAllDay = { form.isAllDay },
            onDatePicked = { isStart, date -> state.edit { it.copy(dates = it.dates.pickDate(isStart, date)) } },
            onTimePicked = { isStart, time -> state.edit { it.copy(dates = it.dates.pickTime(isStart, time)) } },
            onTimeZoneClick = onTimeZoneClick,
            modifier = Modifier.eventSharedBounds(EventSharedElement.DateAndTime),
        )

        Section(EventSharedElement.Divider1) {
            AttendeesButton(
                attendees = { form.attendees },
                onClick = onAttendeesClick,
                modifier = Modifier.eventSharedElement(EventSharedElement.Attendees),
            )

            KMeetButtonSwitch(
                checked = { form.isKMeetUrlEnabled },
                onCheckedChange = { isEnabled -> state.edit { it.copy(isKMeetUrlEnabled = isEnabled) } },
                modifier = Modifier.eventSharedBounds(EventSharedElement.KMeet),
            )
        }

        Section(EventSharedElement.Divider3, Modifier.animateContentSize()) {
            var isReminderOffsetDialogVisible by rememberSaveable { mutableStateOf(false) }

            form.notifications.forEach { notification ->
                ReminderItem(
                    notification = notification,
                    onRemoveClick = { state.edit { it.copy(notifications = it.notifications - notification) } },
                )
            }

            AddReminderButton(onClick = { isReminderOffsetDialogVisible = true })

            if (isReminderOffsetDialogVisible) {
                ReminderOffsetDialog(
                    selectedOffset = null,
                    onOffsetSelect = { offset ->
                        if (offset != null) {
                            val notification = EventDetailUi.Notification(
                                type = EventDetailUi.Notification.Type.Push,
                                time = NotificationTime.Offset(offset),
                            )
                            state.edit { it.copy(notifications = it.notifications + notification) }
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
                checked = { form.isOccupied },
                onCheckedChange = { isOccupied -> state.edit { it.copy(isOccupied = isOccupied) } },
                modifier = Modifier.eventSharedBounds(EventSharedElement.OccupiedStatus),
            )

            ClassificationStatusSwitch(
                checked = { form.isPublic },
                onCheckedChange = { isPublic -> state.edit { it.copy(isPublic = isPublic) } },
                modifier = Modifier.eventSharedBounds(EventSharedElement.ClassificationStatus),
            )

            CalendarEditable(
                selectedCalendar = calendar,
                calendars = calendars,
                onSelect = { calendar -> state.edit { it.withCalendar(calendar) } },
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
            val state = remember {
                EventFormState(
                    EventFormValues.default(
                        calendar = previewEventDetailCalendar,
                        date = Clock.today(),
                        timeZone = TimeZone.currentSystemDefault(),
                        eventDuration = 1.hours,
                    ).copy(
                        title = "Event title",
                        notifications = listOf(
                            EventDetailUi.Notification(EventDetailUi.Notification.Type.Push, NotificationTime.Offset((-15).minutes)),
                        ),
                    ),
                )
            }
            EventForm(
                state = state,
                calendars = previewEventDetailCalendarGroups,
                onAttendeesClick = {},
                onTimeZoneClick = {},
                onCustomReminderClick = {},
                contentPadding = PaddingValues(16.dp),
            )
        }
    }
}
