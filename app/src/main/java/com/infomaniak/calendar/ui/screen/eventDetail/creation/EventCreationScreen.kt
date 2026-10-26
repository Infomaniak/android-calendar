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
package com.infomaniak.calendar.ui.screen.eventDetail.creation

import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.eventdetail.form.EventForm
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.form.rememberDefaultEventForm
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendarGroups
import com.infomaniak.calendar.components.eventdetail.state.rememberDateAndTimeState
import com.infomaniak.calendar.components.foundation.state.rememberSaveableAttendeesSearchState
import com.infomaniak.calendar.ui.component.topAppBar.SubmitTopAppBar
import com.infomaniak.calendar.ui.navigation.state.LocalSharedSnackbarHostState
import com.infomaniak.calendar.ui.screen.eventDetail.SaveEventResult
import com.infomaniak.calendar.ui.screen.eventDetail.attendeesSearch.AttendeesSearchSharedDataViewModel
import com.infomaniak.calendar.ui.screen.eventDetail.timeZoneSearch.TimeZoneSearchResultViewModel
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.ui.theme.Dimens
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Duration.Companion.hours
import com.infomaniak.core.common.R as RCore

@Composable
fun EventCreationScreen(
    date: LocalDate,
    goBack: () -> Unit,
    goToEventsAttendees: () -> Unit,
    goToTimeZoneSearch: (isStart: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventCreationViewModel = viewModel(),
    attendeesSearchSharedDataViewModel: AttendeesSearchSharedDataViewModel = viewModel(),
    timeZoneSearchResultViewModel: TimeZoneSearchResultViewModel = viewModel(),
) {
    val snackbarHostState = LocalSharedSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()
    val errorText = stringResource(RCore.string.anErrorHasOccurred)
    val eventFormCalendars = viewModel.eventFormCalendars.collectAsStateWithLifecycle().value
    val defaultEventDuration by viewModel.defaultEventDuration.collectAsStateWithLifecycle(null)

    // Null while loading, or if the user has no writable calendar: an event can't be created anywhere then
    val state = eventFormCalendars?.let {
        rememberDefaultEventForm(
            calendars = it.calendars,
            initialCalendar = it.initialCalendar,
            dateAndTimeState = defaultEventDuration?.let { rememberDateAndTimeState(it, TimeZone.currentSystemDefault(), date) },
            attendeesSearchState = viewModel.attendeesSearchState,
        )
    }

    LaunchedEffect(state?.dateAndTimeState) {
        timeZoneSearchResultViewModel.applyPickedTimeZonesTo(state?.dateAndTimeState)
    }

    LaunchedEffect(Unit) {
        attendeesSearchSharedDataViewModel.setAttendeesSearchState(viewModel.attendeesSearchState)
    }

    EventCreationScreen(
        state = state,
        onSubmit = {
            val eventDraft = state?.toEventDraft() ?: return@EventCreationScreen
            coroutineScope.launch {
                when (viewModel.submitEvent(eventDraft)) {
                    SaveEventResult.Success -> goBack()
                    SaveEventResult.Error -> snackbarHostState?.showSnackbar(message = errorText)
                }
            }
        },
        modifier = modifier,
        goBack = goBack,
        goToEventsAttendees = goToEventsAttendees,
        goToTimeZoneSearch = goToTimeZoneSearch,
    )
}

@Composable
private fun EventCreationScreen(
    state: EventFormState?,
    onSubmit: () -> Unit,
    goBack: () -> Unit,
    goToEventsAttendees: () -> Unit,
    goToTimeZoneSearch: (isStart: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = { SubmitTopAppBar(goBack = goBack, onSubmit = onSubmit) },
        modifier = modifier,
    ) { contentPadding ->
        if (state == null) return@Scaffold

        EventForm(
            state = state,
            onAttendeesClick = goToEventsAttendees,
            onTimeZoneClick = goToTimeZoneSearch,
            contentPadding = contentPadding + Dimens.EventDetailScreensHorizontalPadding,
            modifier = Modifier.verticalScroll(scrollState),
        )
    }
}

@Preview
@Composable
private fun EventCreationScreenPreview() {
    CalendarThemeForPreview {
        val state = rememberDefaultEventForm(
            calendars = previewEventDetailCalendarGroups,
            initialCalendar = previewEventDetailCalendar,
            dateAndTimeState = rememberDateAndTimeState(1.hours, TimeZone.currentSystemDefault()),
            attendeesSearchState = rememberSaveableAttendeesSearchState(),
        )

        EventCreationScreen(state = state, onSubmit = {}, goBack = {}, goToEventsAttendees = {}, goToTimeZoneSearch = {})
    }
}
