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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.attendeessearch.AttendeesSearch
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.preview.previewAttendees
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.components.foundation.state.rememberSaveableAttendeesSearchState
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.navigation.LocalSharedViewModelStoreOwner
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AttendeesSearchScreen(
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AttendeesSearchSharedDataViewModel = viewModel(LocalSharedViewModelStoreOwner.current), // shared from parent
) {
    val attendeesSearchUi by viewModel.attendeesState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when (val attendeesState = attendeesSearchUi) {
            AttendeesSearchUiState.Loading -> Unit // The parent provides it right away, so nothing to show
            is AttendeesSearchUiState.Success -> {
                val attendeesSearchState = rememberSaveableAttendeesSearchState(
                    attendees = attendeesState.attendees,
                )

                LaunchedEffect(attendeesSearchState) {
                    snapshotFlow { attendeesSearchState.searchQueryTextFieldState.text }.collectLatest {
                        viewModel.search(it.toString())
                    }
                }


                AttendeesSearchScreen(
                    attendeesSearchState = attendeesSearchState,
                    goBack = goBack,
                )
            }
        }
    }
}

@Composable
private fun AttendeesSearchScreen(
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
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
    ) { contentPadding ->
        AttendeesSearch(
            attendeesSearchState = attendeesSearchState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = contentPadding,
        )
    }
}

@Preview
@Composable
private fun EventAttendeesSearchScreenPreview() {
    CalendarThemeForPreview {
        AttendeesSearchScreen(
            attendeesSearchState = rememberSaveableAttendeesSearchState(
                attendees = remember { mutableStateSetOf<AttendeeUi>().apply { addAll(previewAttendees) } },
                results = remember { mutableStateListOf<AttendeeUi>().apply { addAll(previewAttendees) } },
            ),
            goBack = {},
        )
    }
}
