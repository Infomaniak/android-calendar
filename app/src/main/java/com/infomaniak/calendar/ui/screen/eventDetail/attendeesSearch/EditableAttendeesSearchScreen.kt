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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.foundation.models.AttendeesSearchResult
import com.infomaniak.calendar.components.foundation.models.ParticipantUi
import com.infomaniak.calendar.components.foundation.preview.previewAttendees
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.components.foundation.state.rememberSaveableAttendeesSearchState
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.navigation.LocalSharedViewModelStoreOwner
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
fun EditableAttendeesSearchScreen(
    hasWritePermissions: Boolean,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AttendeesSearchSharedDataViewModel = viewModel(LocalSharedViewModelStoreOwner.current), // shared from parent
) {
    val attendeesSearchUi by viewModel.attendeesSearchState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when (val attendeesSearchState = attendeesSearchUi) {
            AttendeesSearchUiState.Loading -> Unit // The parent provides it right away, so nothing to show
            is AttendeesSearchUiState.Success -> {
                if (hasWritePermissions) viewModel.EnableSearch()

                EditableAttendeesSearchScreen(
                    attendeesSearchState = attendeesSearchState.state,
                    hasWritePermissions = hasWritePermissions,
                    onSubmit = {},
                    goBack = goBack,
                )
            }
        }
    }
}

@Composable
private fun EditableAttendeesSearchScreen(
    attendeesSearchState: AttendeesSearchState<*>,
    hasWritePermissions: Boolean,
    onSubmit: () -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialParticipantsSnapshot = rememberSaveable(saver = SetSaver) { attendeesSearchState.participants.toSet() }
    val isSubmitEnabled by remember { derivedStateOf { attendeesSearchState.participants.toSet() != initialParticipantsSnapshot } }

    AttendeesSearchScreenContent(
        topBar = {
            TopAppBar(
                navigationIcon = { TopAppBarButtons.BackButton(onClick = goBack) },
                actions = {
                    if (hasWritePermissions) {
                        Row(Modifier.padding(end = EsdsTheme.spacing.lg)) {
                            TopAppBarButtons.SaveButton(enabled = { isSubmitEnabled }, onClick = onSubmit)
                        }
                    }
                },
                title = { Text(text = stringResource(R.string.attendeesSearchTitle)) },
            )
        },
        attendeesSearchState = attendeesSearchState,
        modifier = modifier,
        isSearchable = hasWritePermissions,
    )
}

private val SetSaver = listSaver<Set<ParticipantUi>, ParticipantUi>(
    save = { it.toList() },
    restore = { it.toSet() },
)

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        EditableAttendeesSearchScreen(
            hasWritePermissions = true,
            attendeesSearchState = rememberSaveableAttendeesSearchState(
                participants = remember { mutableStateSetOf<ParticipantUi>().apply { addAll(previewAttendees) } },
                results = remember { mutableStateListOf<AttendeesSearchResult>().apply { addAll(previewAttendees) } },
            ),
            goBack = {},
            onSubmit = {},
        )
    }
}
