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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.infomaniak.calendar.components.eventdetail.component.ReminderItemEditable
import com.infomaniak.calendar.components.eventdetail.component.Section
import com.infomaniak.calendar.components.eventdetail.component.TitleEditable
import com.infomaniak.calendar.components.eventdetail.component.item
import com.infomaniak.calendar.components.eventdetail.component.reminder.AddReminderButton
import com.infomaniak.calendar.components.eventdetail.component.reminder.ReminderOffsetDialog
import com.infomaniak.calendar.components.eventdetail.form.component.AllDaySwitch
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.ReminderTime
import com.infomaniak.calendar.components.eventdetail.modifier.EventSharedElement
import com.infomaniak.calendar.components.eventdetail.modifier.ProvideEventSharedTransition
import com.infomaniak.calendar.components.eventdetail.modifier.eventSharedBounds
import com.infomaniak.calendar.components.eventdetail.modifier.eventSharedElement
import com.infomaniak.calendar.components.eventdetail.modifier.reminderSharedBounds
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
    onAttendeesClick: () -> Unit,
    onTimeZoneClick: (isStart: Boolean) -> Unit,
    onCustomReminderClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) = ProvideEventSharedTransition(sharedTransitionScope, animatedVisibilityScope) {
    Column(modifier.padding(contentPadding)) {
        val focusRequester = rememberInitialFocusRequester(shouldRequestFocus = { state.titleTextState.text.isEmpty() })
        Section {
            item {
                TitleEditable(
                    dotColor = { state.eventColorState.value.getColor(state.calendar) },
                    shapes = it,
                    textFieldState = state.titleTextState,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .eventSharedElement(EventSharedElement.Title),
                )
            }
        }

        Section {
            item {
                AllDaySwitch(
                    checked = state.isAllDay::value,
                    onCheckedChange = { isChecked -> state.isAllDay.value = isChecked },
                    shapes = it,
                )
            }
        }

        Section {
            item {
                DateAndTimeEditable(
                    dateAndTimeState = state.dateAndTimeState,
                    isAllDay = state.isAllDay::value,
                    onTimeZoneClick = onTimeZoneClick,
                    shapes = it,
                    modifier = Modifier.eventSharedBounds(EventSharedElement.DateAndTime),
                )
            }
        }

        Section {
            item {
                AttendeesButton(
                    attendees = { state.attendees },
                    onClick = onAttendeesClick,
                    shapes = it,
                    modifier = Modifier.eventSharedElement(EventSharedElement.Attendees),
                )
            }

            item {
                KMeetButtonSwitch(
                    shapes = it,
                    checked = state.isKMeetUrlEnabled::value,
                    onCheckedChange = { isChecked -> state.isKMeetUrlEnabled.value = isChecked },
                    modifier = Modifier.eventSharedBounds(EventSharedElement.KMeet),
                )
            }
        }

        var isReminderOffsetDialogVisible by rememberSaveable { mutableStateOf(false) }

        ReminderItems(
            reminders = state.reminders,
            onRemove = { reminder -> state.reminders.remove(reminder) },
            onAddClick = { isReminderOffsetDialogVisible = true },
        )

        ReminderDialog(
            reminders = state.reminders,
            onCustomReminderClick = onCustomReminderClick,
            isReminderOffsetDialogVisible = isReminderOffsetDialogVisible,
            setIsReminderOffsetDialogVisible = { isReminderOffsetDialogVisible = it },
        )

        Section {
            item {
                OccupiedStatusSwitch(
                    checked = state.isOccupied::value,
                    onCheckedChange = { isChecked -> state.isOccupied.value = isChecked },
                    modifier = Modifier.eventSharedBounds(EventSharedElement.OccupiedStatus),
                    shapes = it,
                )
            }

            item {
                ClassificationStatusSwitch(
                    checked = state.isPublic::value,
                    onCheckedChange = { isChecked -> state.isPublic.value = isChecked },
                    modifier = Modifier.eventSharedBounds(EventSharedElement.ClassificationStatus),
                    shapes = it,
                )
            }

            item {
                CalendarEditable(
                    selectedCalendar = state.calendar,
                    calendars = state.calendars,
                    onSelect = state::selectCalendar,
                    modifier = Modifier.eventSharedBounds(EventSharedElement.Calendar),
                    shapes = it,
                )
            }
        }
    }
}

@Composable
private fun ReminderDialog(
    reminders: SnapshotStateList<EventDetailUi.Reminder>,
    onCustomReminderClick: () -> Unit,
    isReminderOffsetDialogVisible: Boolean,
    setIsReminderOffsetDialogVisible: (Boolean) -> Unit,
) {
    if (isReminderOffsetDialogVisible) {
        ReminderOffsetDialog(
            selectedOffset = null,
            onOffsetSelect = { offset ->
                if (offset != null) {
                    reminders.add(
                        EventDetailUi.Reminder(
                            type = EventDetailUi.Reminder.Type.Push,
                            time = ReminderTime.Offset(offset),
                        ),
                    )
                }
                setIsReminderOffsetDialogVisible(false)
            },
            onCustomClick = {
                setIsReminderOffsetDialogVisible(false)
                onCustomReminderClick()
            },
            onDismissRequest = { setIsReminderOffsetDialogVisible(false) },
        )
    }
}

@Composable
private fun ReminderItems(
    reminders: List<EventDetailUi.Reminder>,
    onRemove: (EventDetailUi.Reminder) -> Unit,
    onAddClick: () -> Unit,
) {
    val initialReminders = remember { reminders.toSet() }

    // Removed reminders leave the state at once, but stay rendered until they've animated out
    var lastRenderedReminders by remember { mutableStateOf(reminders.toList()) }
    val renderedReminders = reminders.plusExitingFrom(lastRenderedReminders)
    SideEffect { lastRenderedReminders = renderedReminders }

    Section(Modifier.animateContentSize()) {
        renderedReminders.forEach { reminder ->
            item { shapes ->
                key(reminder) { // Required for proper appearance/disappearance animation
                    AnimatedReminderItem(
                        reminder = reminder,
                        isVisible = reminder in reminders,
                        onRemoveClick = { onRemove(reminder) },
                        onExitFinish = { lastRenderedReminders -= reminder },
                        // Reminders already there on entry don't animate in, nor clash with the shared-element transition
                        animateEnter = reminder !in initialReminders,
                        shapes = shapes,
                    )
                }
            }
        }

        item {
            AddReminderButton(onClick = onAddClick, shapes = it)
        }
    }
}

/** A [ReminderItemEditable] that expands in, and shrinks out when no longer [isVisible], calling [onExitFinish] once gone. */
@Composable
private fun AnimatedReminderItem(
    reminder: EventDetailUi.Reminder,
    isVisible: Boolean,
    onRemoveClick: () -> Unit,
    onExitFinish: () -> Unit,
    shapes: ListItemShapes,
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
        ReminderItemEditable(
            reminder = reminder,
            shapes = shapes,
            onRemoveClick = onRemoveClick,
            modifier = Modifier.reminderSharedBounds(reminder),
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
            val state = remember {
                EventFormState.default(
                    calendar = previewEventDetailCalendar,
                    date = Clock.today(),
                    timeZone = TimeZone.currentSystemDefault(),
                    eventDuration = 1.hours,
                    calendars = previewEventDetailCalendarGroups,
                ).apply {
                    titleTextState.setTextAndPlaceCursorAtEnd("Event title")
                    reminders.add(
                        EventDetailUi.Reminder(EventDetailUi.Reminder.Type.Push, ReminderTime.Offset((-15).minutes)),
                    )
                }
            }
            EventForm(
                state = state,
                onAttendeesClick = {},
                onTimeZoneClick = {},
                onCustomReminderClick = {},
                contentPadding = PaddingValues(16.dp),
            )
        }
    }
}
