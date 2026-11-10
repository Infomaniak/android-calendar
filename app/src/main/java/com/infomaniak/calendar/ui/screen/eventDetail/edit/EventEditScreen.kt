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
package com.infomaniak.calendar.ui.screen.eventDetail.edit

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.infomaniak.calendar.components.eventdetail.form.EventForm
import com.infomaniak.calendar.components.eventdetail.form.EventFormValues
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendarGroups
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.preview.previewAttendees
import com.infomaniak.calendar.ui.component.topAppBar.SubmitTopAppBar
import com.infomaniak.calendar.ui.modifier.LocalSharedTransitionScope
import com.infomaniak.calendar.ui.navigation.LocalSharedViewModelStoreOwner
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailSharedDataViewModel
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.screen.eventDetail.form.EventFormViewModel
import com.infomaniak.calendar.ui.screen.eventDetail.form.eventFormViewModel
import com.infomaniak.calendar.ui.screen.eventDetail.scope.RecurrentEventScope
import com.infomaniak.calendar.ui.theme.CalendarTheme
import com.infomaniak.calendar.ui.theme.Dimens
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@Composable
fun EventEditScreen(
    destination: NavDestination.EventEdit,
    goBack: () -> Unit,
    goBackToCalendarView: () -> Unit,
    goToEventAttendees: () -> Unit,
    goToTimeZoneSearch: (isStart: Boolean) -> Unit,
    goToCustomReminder: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventEditViewModel = viewModel(),
    eventDetailSharedDataViewModel: EventDetailSharedDataViewModel = viewModel(LocalSharedViewModelStoreOwner.current),
    eventFormViewModel: EventFormViewModel = eventFormViewModel(
        destination = destination,
        loadedEvent = (eventDetailSharedDataViewModel.eventDetailUi.value as? EventDetailUiState.Displayable.Success)?.eventDetail,
    ),
) {
    val eventDetailUiState = eventDetailSharedDataViewModel.eventDetailUi.collectAsStateWithLifecycle().value
    val values = eventFormViewModel.values
    val calendars = eventFormViewModel.calendars

    val uiState = if (eventDetailUiState is EventDetailUiState.Displayable.Success && values != null && calendars != null) {
        EventEditScreenState.Success(values, calendars, isRecurring = eventDetailUiState.eventDetail.isRecurring)
    } else {
        EventEditScreenState.Loading
    }

    LaunchedEffect(destination.occurrenceId) {
        eventDetailSharedDataViewModel.setOccurrenceId(destination.occurrenceId)
    }

    LaunchedEffect(Unit) {
        for (request in eventDetailSharedDataViewModel.goBackRequests) goBackToCalendarView()
    }

    EventEditScreen(
        uiState = uiState,
        attendees = { eventFormViewModel.attendeesSearchState?.attendees.orEmpty() },
        update = eventFormViewModel::update,
        goBack = goBack,
        goToEventAttendees = goToEventAttendees,
        goToTimeZoneSearch = goToTimeZoneSearch,
        goToCustomReminder = goToCustomReminder,
        modifier = modifier,
        onSubmit = { scope ->
            val eventDraft = eventFormViewModel.toEventDraft() ?: return@EventEditScreen
            if (viewModel.submitEvent(destination.occurrenceId, scope, eventDraft)) goBack()
        },
        sharedTransitionScope = LocalSharedTransitionScope.current,
        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
    )
}

@Composable
private fun EventEditScreen(
    uiState: EventEditScreenState,
    attendees: () -> List<AttendeeUi>,
    update: ((EventFormValues) -> EventFormValues) -> Unit,
    goBack: () -> Unit,
    goToEventAttendees: () -> Unit,
    goToTimeZoneSearch: (isStart: Boolean) -> Unit,
    goToCustomReminder: () -> Unit,
    onSubmit: (RecurrentEventScope) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SubmitTopAppBar(
                goBack = goBack,
                onSubmit = onSubmit,
                isRecurring = (uiState as? EventEditScreenState.Success)?.isRecurring ?: false,
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        when (uiState) {
            // Coming from the detail screen, the shared flow is already warm, so this is only hit when entering edit directly or
            // recreating the activity, but it still will be loaded from disk which is fast enough.
            EventEditScreenState.Loading -> Unit
            is EventEditScreenState.Success -> {
                EventForm(
                    values = uiState.values,
                    update = update,
                    calendars = uiState.calendars,
                    attendees = attendees,
                    onAttendeesClick = goToEventAttendees,
                    onTimeZoneClick = goToTimeZoneSearch,
                    onCustomReminderClick = goToCustomReminder,
                    modifier = Modifier.verticalScroll(scrollState),
                    contentPadding = contentPadding + Dimens.EventDetailScreensHorizontalPadding,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        }
    }
}

private sealed interface EventEditScreenState {
    object Loading : EventEditScreenState
    data class Success(
        val values: EventFormValues,
        val calendars: List<EventDetailCalendarGroup>,
        val isRecurring: Boolean,
    ) : EventEditScreenState
}

@Preview
@Composable
private fun Preview() {
    CalendarTheme {
        Surface {
            var values by remember {
                mutableStateOf(
                    EventFormValues.default(
                        calendarId = previewEventDetailCalendar.id,
                        date = Clock.today(),
                        timeZone = TimeZone.currentSystemDefault(),
                        eventDuration = 1.hours,
                    ).copy(title = "Meeting on how to find funny preview titles"),
                )
            }

            EventEditScreen(
                uiState = EventEditScreenState.Success(values, previewEventDetailCalendarGroups, isRecurring = false),
                attendees = { previewAttendees },
                update = { transform -> values = transform(values) },
                goBack = {},
                goToEventAttendees = {},
                goToTimeZoneSearch = {},
                goToCustomReminder = {},
                onSubmit = {},
            )
        }
    }
}
