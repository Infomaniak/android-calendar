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
package com.infomaniak.calendar.ui.screen.attendeesSearch

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.attendeessearch.AttendeesSearch
import com.infomaniak.calendar.components.attendeessearch.state.AttendeesState
import com.infomaniak.calendar.components.attendeessearch.state.rememberAttendeesState
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.utils.toAttendeeUi
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId

@Composable
fun EventAttendeesScreen(
    occurrenceId: OccurrenceId,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventAttendeesViewModel = viewModel(),
) {
    val state by viewModel.eventAttendeesState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setOccurrenceId(occurrenceId)
    }

    when (val currentState = state) {
        EventAttendeesUiState.Loading -> Unit // TODO: check how we want to handle the loading state while searching
        EventAttendeesUiState.EventMissing -> Unit
        is EventAttendeesUiState.Loaded -> {
            val attendeesState = rememberAttendeesState(
                attendees = currentState.attendees.map { it.toAttendeeUi() },
                contacts = currentState.contacts.map { it.toAttendeeUi() },
            )

            EventAttendeesScreen(
                attendeesState = attendeesState,
                goBack = goBack,
                modifier = modifier,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventAttendeesScreen(
    attendeesState: AttendeesState,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { TopAppBarButtons.BackButton(onClick = goBack) },
                title = { Text(text = stringResource(R.string.attendeesTitle)) },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        AttendeesSearch(
            attendees = { attendeesState.searchResults },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth(),
            searchQuery = { attendeesState.searchQuery },
            onSearchQueryChanged = attendeesState::onSearchQueryChanged,
        )
    }
}
