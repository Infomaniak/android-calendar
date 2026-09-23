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
package com.infomaniak.calendar.ui.screen.eventDetail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.eventdetail.AttendeesSearch
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview

@Composable
fun EventAttendeesScreen(
    eventId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventDetailViewModel = viewModel(),
) {
    val state by viewModel.eventAttendeesState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (state is EventAttendeesUiState.EventMissing) {
            onBack()
        }
        viewModel.setEventId(eventId)
    }

    when (val currentState = state) {
        EventAttendeesUiState.Loading -> Unit // TODO: check how we want to handle the loading state while searching
        EventAttendeesUiState.EventMissing -> Unit
        is EventAttendeesUiState.Loaded -> {
            EventAttendeesScreen(
                attendees = { currentState.attendees },
                searchQuery = { searchQuery },
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onBack = onBack,
                modifier = modifier,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventAttendeesScreen(
    attendees: () -> List<AttendeeUi>,
    searchQuery: () -> String,
    onSearchQueryChanged: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { TopAppBarButtons.BackButton(onClick = onBack) },
                title = { Text(text = stringResource(R.string.attendeesTitle)) },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        AttendeesSearch(
            attendees = attendees,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth(),
            searchQuery = searchQuery,
            onSearchQueryChanged = onSearchQueryChanged,
        )
    }
}

@Preview
@Composable
private fun EventAttendeesScreenPreview() {
    val previewAttendees = listOf(
        AttendeeUi(
            email = "alice@example.com",
            displayName = "Alice Johnson",
            isOrganizer = true,
            status = ParticipationStatus.Accepted,
        ),
        AttendeeUi(
            email = "bob@example.com",
            displayName = "Bob Smith",
            status = ParticipationStatus.Tentative,
        ),
        AttendeeUi(
            email = "charlie@example.com",
            displayName = "Charlie Brown",
            status = ParticipationStatus.Declined,
        ),
    )

    CalendarThemeForPreview {
        Surface {
            EventAttendeesScreen(
                attendees = { previewAttendees },
                searchQuery = { "" },
                onSearchQueryChanged = {},
                onBack = {},
            )
        }
    }
}
