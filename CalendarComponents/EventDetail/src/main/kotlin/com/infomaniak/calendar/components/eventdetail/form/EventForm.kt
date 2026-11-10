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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import com.infomaniak.calendar.components.eventdetail.modifier.reminderSharedBounds
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendarGroups
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
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
    values: EventFormValues,
    update: ((EventFormValues) -> EventFormValues) -> Unit,
    calendars: List<EventDetailCalendarGroup>,
    attendees: () -> List<AttendeeUi>,
    onAttendeesClick: () -> Unit,
    onTimeZoneClick: (isStart: Boolean) -> Unit,
    onCustomReminderClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) = ProvideEventSharedTransition(sharedTransitionScope, animatedVisibilityScope) {
    val calendar = calendars.calendarOf(values.calendarId) ?: return@ProvideEventSharedTransition

    val titleState = rememberTextFieldState(values.title)
    val currentUpdate by rememberUpdatedState(update)
    LaunchedEffect(titleState) {
        snapshotFlow { titleState.text.toString() }.collect { title -> currentUpdate { it.copy(title = title) } }
    }

    Column(modifier.padding(contentPadding)) {
        val focusRequester = rememberInitialFocusRequester(shouldRequestFocus = { titleState.text.isEmpty() })
        TitleEditable(
            dotColor = { values.eventColor.getColor(calendar) },
            textFieldState = titleState,
            modifier = Modifier
                .focusRequester(focusRequester)
                .eventSharedElement(EventSharedElement.Title),
        )

        AllDaySwitch(
            checked = { values.isAllDay },
            onCheckedChange = { isChecked -> update { it.copy(isAllDay = isChecked) } },
        )

        DateAndTimeEditable(
            dates = values.dates,
            isAllDay = { values.isAllDay },
            onDatesChange = { transform -> update { it.copy(dates = transform(it.dates)) } },
            onTimeZoneClick = onTimeZoneClick,
            modifier = Modifier.eventSharedBounds(EventSharedElement.DateAndTime),
        )

        Section(EventSharedElement.Divider1) {
            AttendeesButton(
                attendees = attendees,
                onClick = onAttendeesClick,
                modifier = Modifier.eventSharedElement(EventSharedElement.Attendees),
            )

            KMeetButtonSwitch(
                checked = { values.isKMeetUrlEnabled },
                onCheckedChange = { isChecked -> update { it.copy(isKMeetUrlEnabled = isChecked) } },
                modifier = Modifier.eventSharedBounds(EventSharedElement.KMeet),
            )
        }

        Section(EventSharedElement.Divider3) {
            var isReminderOffsetDialogVisible by rememberSaveable { mutableStateOf(false) }
            ReminderItems(
                notifications = values.notifications,
                onRemove = { notification -> update { it.copy(notifications = it.notifications - notification) } },
            )

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
                            update { it.copy(notifications = it.notifications + notification) }
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
                checked = { values.isOccupied },
                onCheckedChange = { isChecked -> update { it.copy(isOccupied = isChecked) } },
                modifier = Modifier.eventSharedBounds(EventSharedElement.OccupiedStatus),
            )

            ClassificationStatusSwitch(
                checked = { values.isPublic },
                onCheckedChange = { isChecked -> update { it.copy(isPublic = isChecked) } },
                modifier = Modifier.eventSharedBounds(EventSharedElement.ClassificationStatus),
            )

            CalendarEditable(
                selectedCalendar = calendar,
                calendars = calendars,
                onSelect = { selected -> update { it.copy(calendarId = selected.id) } },
                modifier = Modifier.eventSharedBounds(EventSharedElement.Calendar),
            )
        }
    }
}

@Composable
private fun ReminderItems(notifications: List<EventDetailUi.Notification>, onRemove: (EventDetailUi.Notification) -> Unit) {
    val initialNotifications = remember { notifications.toSet() }

    // Removed reminders leave the state at once, but stay rendered until they've animated out
    var lastRenderedNotifications by remember { mutableStateOf(notifications) }
    val renderedNotifications = notifications.plusExitingFrom(lastRenderedNotifications)
    SideEffect { lastRenderedNotifications = renderedNotifications }

    renderedNotifications.forEach { notification ->
        key(notification) { // Required for proper appearance/disappearance animation
            AnimatedReminderItem(
                notification = notification,
                isVisible = notification in notifications,
                onRemoveClick = { onRemove(notification) },
                onExitFinish = { lastRenderedNotifications -= notification },
                // Reminders already there on entry don't animate in, nor clash with the shared-element transition
                animateEnter = notification !in initialNotifications,
            )
        }
    }
}

/** A [ReminderItem] that expands in, and shrinks out when no longer [isVisible], calling [onExitFinish] once gone. */
@Composable
private fun AnimatedReminderItem(
    notification: EventDetailUi.Notification,
    isVisible: Boolean,
    onRemoveClick: () -> Unit,
    onExitFinish: () -> Unit,
    modifier: Modifier = Modifier,
    animateEnter: Boolean = true,
) {
    val visibility = remember { MutableTransitionState(!animateEnter) }
    visibility.targetState = isVisible

    AnimatedVisibility(
        visibleState = visibility,
        modifier = modifier,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        ReminderItem(
            notification = notification,
            onRemoveClick = onRemoveClick,
            modifier = Modifier.reminderSharedBounds(notification),
        )
    }

    LaunchedEffect(visibility.isIdle) {
        if (visibility.isIdle && !visibility.targetState) onExitFinish()
    }
}

/** This list, plus the items of [previous] it no longer has (the ones exiting), put back at their former index. */
private fun <T> List<T>.plusExitingFrom(previous: List<T>): List<T> {
    val result = toMutableList()
    previous.forEachIndexed { index, item ->
        if (item !in this) result.add(index.coerceAtMost(result.size), item)
    }
    return result
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            var values by remember {
                mutableStateOf(
                    EventFormValues.default(
                        calendarId = previewEventDetailCalendar.id,
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
                values = values,
                update = { transform -> values = transform(values) },
                calendars = previewEventDetailCalendarGroups,
                attendees = { emptyList() },
                onAttendeesClick = {},
                onTimeZoneClick = {},
                onCustomReminderClick = {},
                contentPadding = PaddingValues(16.dp),
            )
        }
    }
}
