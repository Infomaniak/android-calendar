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
package com.infomaniak.calendar.ui.screen.eventDetail.attendeesSearch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.attendeessearch.AttendeesSearch
import com.infomaniak.calendar.components.attendeessearch.state.AttendeesSearchState
import com.infomaniak.calendar.components.attendeessearch.state.rememberSaveableAttendeesSearchState
import com.infomaniak.calendar.components.foundation.preview.previewAttendees
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.navigation.state.LocalSharedSnackbarHostState
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId

@Composable
fun EventAttendeesScreen(
    occurrenceId: OccurrenceId,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventAttendeesViewModel = viewModel(),
) {
    val state = viewModel.eventDetailUi.collectAsStateWithLifecycle().value

    LaunchedEffect(occurrenceId) {
        viewModel.setOccurrenceId(occurrenceId)
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            EventDetailUiState.Loading -> Unit // This happens too quickly, so no need to show anything
            EventDetailUiState.Unavailable -> {
                val snackbarHostState = LocalSharedSnackbarHostState.current
                val eventMissingMessage = stringResource(R.string.eventOccurrenceNotFound)

                LaunchedEffect(occurrenceId) {
                    snackbarHostState?.showSnackbar(eventMissingMessage)
                    goBack()
                }
            }
            is EventDetailUiState.Success -> {
                val attendeesSearchState = rememberSaveableAttendeesSearchState(
                    attendees = state.eventDetail.attendees.all,
                    contacts = state.eventDetail.attendees.all,
                )

                EventAttendeesScreen(
                    attendeesSearchState = attendeesSearchState,
                    goBack = goBack,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventAttendeesScreen(
    attendeesSearchState: AttendeesSearchState,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { TopAppBarButtons.BackButton(onClick = goBack) },
                title = { Text(text = stringResource(R.string.attendeesSearchTitle)) },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        AttendeesSearch(
            attendeesSearchState = attendeesSearchState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun EventAttendeesSearchScreenPreview() {
    CalendarThemeForPreview {
        EventAttendeesScreen(
            attendeesSearchState = rememberSaveableAttendeesSearchState(
                attendees = previewAttendees,
                contacts = previewAttendees,
            ),
            goBack = {},
        )
    }
}
