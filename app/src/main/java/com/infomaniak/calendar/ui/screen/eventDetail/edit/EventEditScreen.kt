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
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.infomaniak.calendar.components.eventdetail.form.EventForm
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.form.rememberEventFormState
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendarGroups
import com.infomaniak.calendar.components.eventdetail.state.DateAndTimeState
import com.infomaniak.calendar.components.eventdetail.state.rememberDateAndTimeState
import com.infomaniak.calendar.components.foundation.preview.previewAttendees
import com.infomaniak.calendar.components.foundation.state.rememberSaveableAttendeesSearchState
import com.infomaniak.calendar.ui.component.topAppBar.SubmitTopAppBar
import com.infomaniak.calendar.ui.modifier.LocalSharedTransitionScope
import com.infomaniak.calendar.ui.navigation.LocalSharedViewModelStoreOwner
import com.infomaniak.calendar.ui.navigation.state.LocalSharedSnackbarHostState
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailSharedDataViewModel
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.screen.eventDetail.SaveEventResult
import com.infomaniak.calendar.ui.screen.eventDetail.attendeesSearch.AttendeesSearchSharedDataViewModel
import com.infomaniak.calendar.ui.screen.eventDetail.scope.RecurrentEventScope
import com.infomaniak.calendar.ui.screen.eventDetail.timeZoneSearch.TimeZoneSearchResultViewModel
import com.infomaniak.calendar.ui.theme.CalendarTheme
import com.infomaniak.calendar.ui.theme.Dimens
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlin.time.Duration.Companion.hours
import com.infomaniak.core.common.R as RCore

@Composable
fun EventEditScreen(
    occurrenceId: OccurrenceId,
    goBack: () -> Unit,
    goBackToCalendarView: () -> Unit,
    goToEventAttendees: () -> Unit,
    goToTimeZoneSearch: (isStart: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventEditViewModel = viewModel(),
    eventDetailSharedDataViewModel: EventDetailSharedDataViewModel = viewModel(LocalSharedViewModelStoreOwner.current),
    attendeesSearchSharedDataViewModel: AttendeesSearchSharedDataViewModel = viewModel(),
    timeZoneSearchResultViewModel: TimeZoneSearchResultViewModel = viewModel(),
) {
    val snackbarHostState = LocalSharedSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()
    val errorText = stringResource(RCore.string.anErrorHasOccurred)
    val eventDetailUiState = eventDetailSharedDataViewModel.eventDetailUi.collectAsStateWithLifecycle().value

    val uiState = when (eventDetailUiState) {
        EventDetailUiState.Displayable.Loading -> EventEditScreenState.Loading
        is EventDetailUiState.Displayable.Success -> EventEditScreenState.Success(
            eventFormState = rememberEventFormState(
                calendars = eventDetailUiState.eventDetail.calendars,
                initialEventColor = eventDetailUiState.eventDetail.eventColor,
                initialCalendar = eventDetailUiState.eventDetail.calendar,
                dateAndTimeState = rememberEventDateState(eventDetailUiState.eventDetail),
                initialText = eventDetailUiState.eventDetail.title,
                initialIsAllDay = eventDetailUiState.eventDetail.isAllDay,
                initialIsKMeetUrlEnabled = eventDetailUiState.eventDetail.kMeetUrl != null,
                attendeesSearchState = viewModel.attendeesSearchState,
                initialIsOccupied = eventDetailUiState.eventDetail.isOccupied,
                initialIsPublic = eventDetailUiState.eventDetail.classification == EventDetailUi.Classification.Public,
            ),
            isRecurrent = eventDetailUiState.eventDetail.isRecurrent,
        )
    }

    LaunchedEffect(occurrenceId) {
        eventDetailSharedDataViewModel.setOccurrenceId(occurrenceId)
    }

    LaunchedEffect(Unit) {
        viewModel.initAttendeesSearchState(eventDetailSharedDataViewModel.eventDetailUi)
    }

    LaunchedEffect(Unit) {
        for (request in eventDetailSharedDataViewModel.goBackRequests) goBackToCalendarView()
    }

    val dateAndTimeState = (uiState as? EventEditScreenState.Success)?.eventFormState?.dateAndTimeState
    LaunchedEffect(dateAndTimeState) {
        timeZoneSearchResultViewModel.applyPickedTimeZonesTo(dateAndTimeState)
    }

    LaunchedEffect(viewModel.attendeesSearchState) {
        attendeesSearchSharedDataViewModel.setAttendeesSearchState(viewModel.attendeesSearchState)
    }

    EventEditScreen(
        uiState = uiState,
        goBack = goBack,
        goToEventAttendees = goToEventAttendees,
        goToTimeZoneSearch = goToTimeZoneSearch,
        modifier = modifier,
        onSubmit = { scope ->
            val eventDraft = (uiState as? EventEditScreenState.Success)?.eventFormState?.toEventDraft() ?: return@EventEditScreen
            coroutineScope.launch {
                when (viewModel.submitEvent(occurrenceId, scope, eventDraft)) {
                    SaveEventResult.Success -> goBack()
                    SaveEventResult.Error -> snackbarHostState?.showSnackbar(message = errorText)
                }
            }
        },
        sharedTransitionScope = LocalSharedTransitionScope.current,
        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
    )
}

@Composable
private fun EventEditScreen(
    uiState: EventEditScreenState,
    goBack: () -> Unit,
    goToEventAttendees: () -> Unit,
    goToTimeZoneSearch: (isStart: Boolean) -> Unit,
    onSubmit: (RecurrentEventScope) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val scrollState = rememberScrollState()
    var showEditRecurrentEventDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SubmitTopAppBar(
                goBack = goBack,
                onSubmit = {
                    val isRecurrent = (uiState as? EventEditScreenState.Success)?.isRecurrent == true
                    if (isRecurrent) showEditRecurrentEventDialog = true else onSubmit(RecurrentEventScope.ThisEvent)
                },
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
                    state = uiState.eventFormState,
                    onAttendeesClick = goToEventAttendees,
                    onTimeZoneClick = goToTimeZoneSearch,
                    modifier = Modifier.verticalScroll(scrollState),
                    contentPadding = contentPadding + Dimens.EventDetailScreensHorizontalPadding,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )

                if (showEditRecurrentEventDialog) {
                    EditRecurrentEventDialog(onEdit = onSubmit, onDismissRequest = { showEditRecurrentEventDialog = false })
                }
            }
        }
    }
}

@Composable
private fun rememberEventDateState(eventDetailUi: EventDetailUi): DateAndTimeState {
    val start = eventDetailUi.start.atTimeZone
    val end = eventDetailUi.end.atTimeZone
    // KMP ends an all-day event on the day after it, the form shows its last day
    val endDate = if (eventDetailUi.isAllDay) end.date.minus(1, DateTimeUnit.DAY) else end.date

    return rememberDateAndTimeState(
        startDate = start.date,
        startTime = start.time,
        startTimeZone = eventDetailUi.start.timeZone,
        endDate = endDate,
        endTime = end.time,
        endTimeZone = eventDetailUi.end.timeZone,
    )
}

private val EventDetailTiming.timeZone: TimeZone
    get() = (this as? EventDetailTiming.Precise)?.timeZone ?: TimeZone.currentSystemDefault()

private sealed interface EventEditScreenState {
    object Loading : EventEditScreenState
    data class Success(val eventFormState: EventFormState, val isRecurrent: Boolean) : EventEditScreenState
}

@Preview
@Composable
private fun Preview() {
    CalendarTheme {
        Surface {
            EventEditScreen(
                uiState = EventEditScreenState.Success(
                    eventFormState = rememberEventFormState(
                        calendars = previewEventDetailCalendarGroups,
                        initialEventColor = previewEventDetailCalendar.color,
                        initialCalendar = previewEventDetailCalendar,
                        dateAndTimeState = rememberDateAndTimeState(1.hours, TimeZone.currentSystemDefault()),
                        initialText = "Meeting on how to find funny preview titles",
                        attendeesSearchState = rememberSaveableAttendeesSearchState(
                            attendees = remember { mutableStateSetOf(*previewAttendees.toTypedArray()) },
                        ),
                        initialIsAllDay = false,
                        initialIsKMeetUrlEnabled = false,
                        initialIsOccupied = true,
                        initialIsPublic = true,
                    ),
                    isRecurrent = false,
                ),
                goBack = {},
                goToEventAttendees = {},
                goToTimeZoneSearch = {},
                onSubmit = {},
            )
        }
    }
}
