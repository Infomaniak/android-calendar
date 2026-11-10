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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.attendeessearch.AttendeesSearch
import com.infomaniak.calendar.components.foundation.models.AttendeesSearchResult
import com.infomaniak.calendar.components.foundation.models.ParticipantUi
import com.infomaniak.calendar.components.foundation.preview.previewAttendees
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.components.foundation.state.rememberSaveableAttendeesSearchState
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.navigation.sharedEventFormViewModel
import com.infomaniak.calendar.ui.navigation.state.LocalSharedSnackbarHostState
import com.infomaniak.calendar.ui.screen.eventDetail.form.EventFormViewModel
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview

@Composable
fun AttendeesSearchScreen(
    parent: NavDestination.EventForm,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AttendeesSearchViewModel = viewModel(),
    eventFormViewModel: EventFormViewModel = sharedEventFormViewModel(parent),
) {
    val formState = eventFormViewModel.formState ?: return
    val attendeesSearchState = rememberSaveableAttendeesSearchState(
        participants = remember { mutableStateSetOf<ParticipantUi>().apply { addAll(formState.form.participants) } },
        organizerSource = formState.form.organizerSource,
    )

    viewModel.EnableSearch(attendeesSearchState)

    LaunchedEffect(attendeesSearchState) {
        snapshotFlow { attendeesSearchState.participants.toSet() }.collect { participants ->
            formState.edit { it.copy(participants = participants) }
        }
    }

    AttendeesSearchScreenContent(
        topBar = { TopBar(goBack, { attendeesSearchState.attendees.size }) },
        attendeesSearchState = attendeesSearchState,
        modifier = modifier,
    )
}

@Composable
fun AttendeesSearchScreenContent(
    attendeesSearchState: AttendeesSearchState<*>,
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isSearchable: Boolean = true,
) {
    Scaffold(
        topBar = topBar,
        modifier = modifier,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
    ) { contentPadding ->
        val snackbarHostState = LocalSharedSnackbarHostState.current

        AttendeesSearch(
            attendeesSearchState = attendeesSearchState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = contentPadding,
            onShowSnackbar = { message -> snackbarHostState?.showSnackbar(message = message) },
            isSearchable = isSearchable,
        )
    }
}

@Composable
private fun TopBar(goBack: () -> Unit, attendeeCount: () -> Int) {
    val count = attendeeCount()
    TopAppBar(
        navigationIcon = { TopAppBarButtons.BackButton(onClick = goBack) },
        title = { Text(text = pluralStringResource(R.plurals.attendeesCount, count, count)) },
    )
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        AttendeesSearchScreenContent(
            topBar = { TopBar({}, { previewAttendees.size }) },
            attendeesSearchState = rememberSaveableAttendeesSearchState(
                participants = remember { mutableStateSetOf<ParticipantUi>().apply { addAll(previewAttendees) } },
                results = remember { mutableStateListOf<AttendeesSearchResult>().apply { addAll(previewAttendees) } },
            ),
        )
    }
}
