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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.eventdetail.form.EventForm
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendarGroups
import com.infomaniak.calendar.components.foundation.component.EmptyState
import com.infomaniak.calendar.ui.component.topAppBar.SubmitTopAppBar
import com.infomaniak.calendar.ui.screen.eventDetail.form.EventFormViewModel
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.ui.theme.Dimens
import com.infomaniak.core.common.utils.today
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CalendarCross
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@Composable
fun EventCreationScreen(
    date: LocalDate,
    goBack: () -> Unit,
    goToEventsAttendees: () -> Unit,
    goToTimeZoneSearch: (isStart: Boolean) -> Unit,
    goToCustomReminder: () -> Unit,
    goToLocationSearch: () -> Unit,
    modifier: Modifier = Modifier,
    eventCreationViewModel: EventCreationViewModel = viewModel(),
    eventFormViewModel: EventFormViewModel = viewModel(),
) {
    val calendarsUiState by eventCreationViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        eventFormViewModel.initCreation(date)
    }

    val uiState = when (calendarsUiState) {
        EventCreationUiState.Loading -> EventCreationScreenState.Loading
        EventCreationUiState.NoWritableCalendar -> EventCreationScreenState.NoWritableCalendar
        is EventCreationUiState.Success -> {
            eventFormViewModel.formState?.let(EventCreationScreenState::Success) ?: EventCreationScreenState.Loading
        }
    }

    EventCreationScreen(
        uiState = uiState,
        onSubmit = {
            val eventDraft = eventFormViewModel.formState?.toEventDraft() ?: return@EventCreationScreen
            if (eventCreationViewModel.submitEvent(eventDraft)) goBack()
        },
        modifier = modifier,
        goBack = goBack,
        goToEventsAttendees = goToEventsAttendees,
        goToTimeZoneSearch = goToTimeZoneSearch,
        goToCustomReminder = goToCustomReminder,
        goToLocationSearch = goToLocationSearch,
    )
}

@Composable
private fun EventCreationScreen(
    uiState: EventCreationScreenState,
    onSubmit: () -> Unit,
    goBack: () -> Unit,
    goToEventsAttendees: () -> Unit,
    goToTimeZoneSearch: (isStart: Boolean) -> Unit,
    goToCustomReminder: () -> Unit,
    goToLocationSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SubmitTopAppBar(
                goBack = goBack,
                onSubmit = { onSubmit() },
                isSubmitEnabled = { uiState is EventCreationScreenState.Success },
                isRecurring = false,
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        when (uiState) {
            EventCreationScreenState.Loading -> Unit
            EventCreationScreenState.NoWritableCalendar -> EmptyState(
                text = stringResource(R.string.noWritableCalendar),
                icon = EsdsIcons.CalendarCross,
                modifier = Modifier.padding(contentPadding),
            )
            is EventCreationScreenState.Success -> EventForm(
                state = uiState.eventFormState,
                onAttendeesClick = goToEventsAttendees,
                onLocationClick = goToLocationSearch,
                onTimeZoneClick = goToTimeZoneSearch,
                onCustomReminderClick = goToCustomReminder,
                contentPadding = contentPadding + Dimens.EventDetailScreensHorizontalPadding,
                modifier = Modifier.verticalScroll(scrollState),
            )
        }
    }
}

private sealed interface EventCreationScreenState {
    data object Loading : EventCreationScreenState
    data object NoWritableCalendar : EventCreationScreenState
    data class Success(val eventFormState: EventFormState) : EventCreationScreenState
}

@Preview
@Composable
private fun EventCreationScreenPreview() {
    CalendarThemeForPreview {
        val state = remember {
            EventFormState.default(
                calendar = previewEventDetailCalendar,
                date = Clock.today(),
                timeZone = TimeZone.currentSystemDefault(),
                eventDuration = 1.hours,
                calendars = previewEventDetailCalendarGroups,
            )
        }

        EventCreationScreen(
            uiState = EventCreationScreenState.Success(state),
            onSubmit = {},
            goBack = {},
            goToEventsAttendees = {},
            goToTimeZoneSearch = {},
            goToCustomReminder = {},
            goToLocationSearch = {},
        )
    }
}

@Preview
@Composable
private fun PreviewEmptyWritableCalendar() {
    CalendarThemeForPreview {
        EventCreationScreen(
            uiState = EventCreationScreenState.NoWritableCalendar,
            onSubmit = {},
            goBack = {},
            goToEventsAttendees = {},
            goToTimeZoneSearch = {},
            goToCustomReminder = {},
            goToLocationSearch = {},
        )
    }
}
