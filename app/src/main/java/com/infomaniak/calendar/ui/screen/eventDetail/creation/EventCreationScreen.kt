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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.eventdetail.form.EventForm
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.form.rememberSaveableEventFormState
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.screen.eventDetail.attendeesSearch.EventAttendeesViewModel
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.ui.theme.Dimens

@Composable
fun EventCreationScreen(
    goBack: () -> Unit,
    goToEventsAttendees: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventCreationViewModel = viewModel(),
    eventAttendeesViewModel: EventAttendeesViewModel = viewModel(),
) {
    val eventFormCalendars = viewModel.eventFormCalendars.collectAsStateWithLifecycle().value
    val state = rememberSaveableEventFormState(
        calendars = eventFormCalendars?.calendars ?: emptyList(),
        initialCalendar = eventFormCalendars?.initialCalendar,
        initialAttendees = viewModel.attendeesSearchState.attendees,
        attendeesSearchState = viewModel.attendeesSearchState,
    )

    LaunchedEffect(Unit) {
        eventAttendeesViewModel.setAttendeesSearchState(viewModel.attendeesSearchState)
    }

    EventCreationScreen(
        state = state,
        onSubmit = { viewModel.submitEvent(state.toEventDraft() ?: return@EventCreationScreen) },
        modifier = modifier,
        goBack = goBack,
        goToEventsAttendees = goToEventsAttendees,
    )
}

@Composable
private fun EventCreationScreen(
    state: EventFormState,
    onSubmit: () -> Unit,
    goBack: () -> Unit,
    goToEventsAttendees: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { TopAppBarButtons.BackButton(onClick = goBack) },
                title = {},
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        EventForm(
            state = state,
            onAttendeesClick = goToEventsAttendees,
            contentPadding = contentPadding + Dimens.EventDetailScreensHorizontalPadding,
        )
    }
}

@Preview
@Composable
private fun EventCreationScreenPreview() {
    CalendarThemeForPreview {
        val state = rememberSaveableEventFormState(
            calendars = listOf(previewEventDetailCalendar),
            initialCalendar = previewEventDetailCalendar,
            initialAttendees = remember { mutableStateSetOf() },
            attendeesSearchState = AttendeesSearchState(
                searchQueryTextFieldState = TextFieldState(),
                attendees = remember { mutableStateSetOf() },
                results = remember { mutableStateListOf() },
            ),
        )

        EventCreationScreen(state = state, onSubmit = {}, goBack = {}, goToEventsAttendees = {})
    }
}
