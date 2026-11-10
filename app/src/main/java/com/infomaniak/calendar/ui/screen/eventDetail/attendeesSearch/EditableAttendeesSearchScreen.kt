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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.foundation.models.AttendeesSearchResult
import com.infomaniak.calendar.components.foundation.models.OrganizerSource
import com.infomaniak.calendar.components.foundation.models.ParticipantUi
import com.infomaniak.calendar.components.foundation.preview.previewAttendees
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.components.foundation.state.rememberSaveableAttendeesSearchState
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons.SubmitButton
import com.infomaniak.calendar.ui.navigation.LocalSharedViewModelStoreOwner
import com.infomaniak.calendar.ui.screen.eventDetail.AttendeesUpdatePayload
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailSharedDataViewModel
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.screen.eventDetail.model.EditAttendeesConfig
import com.infomaniak.calendar.ui.screen.eventDetail.scope.RecurrentEventScope
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId

@Composable
fun EditableAttendeesSearchScreen(
    config: EditAttendeesConfig,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditableAttendeesSearchViewModel = viewModel(),
    searchViewModel: AttendeesSearchViewModel = viewModel(),
    eventDetailSharedDataViewModel: EventDetailSharedDataViewModel = viewModel(LocalSharedViewModelStoreOwner.current),
) {
    val eventDetailUi by eventDetailSharedDataViewModel.eventDetailUi.collectAsState()

    when (eventDetailUi) {
        EventDetailUiState.Displayable.Loading -> Unit // The parent provides it right away, so nothing to show
        is EventDetailUiState.Displayable.Success -> {
            val attendeesSearchState = rememberSaveableAttendeesSearchState(
                rememberSaveable { mutableStateSetOf<ParticipantUi>(eventDetailUi.eventDetail.attendees.toAttendeesUi()) },
            )

            if (config.canEdit) searchViewModel.EnableSearch(attendeesSearchState)

            EditableAttendeesSearchScreen(
                attendeesSearchState = attendeesSearchState,
                config = config,
                onSubmit = { scope ->
                    val isSuccess = viewModel.updateAttendees(
                        occurrenceId = config.occurrenceId,
                        attendeesUpdatePayload = AttendeesUpdatePayload(
                            attendees = attendeesSearchState.attendees,
                            organizer = (attendeesSearchState.organizerSource as? OrganizerSource.Static)?.organizer,
                            calendarId = CalendarId(config.calendarId),
                        ),
                        scope = scope,
                    )
                    if (isSuccess) goBack()
                },
                goBack = goBack,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun EditableAttendeesSearchScreen(
    attendeesSearchState: AttendeesSearchState<*>,
    config: EditAttendeesConfig,
    onSubmit: (RecurrentEventScope) -> Unit,
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
                    if (config.canEdit) {
                        Row(Modifier.padding(end = EsdsTheme.spacing.lg)) {
                            SubmitButton(
                                isRecurring = config.isRecurring,
                                onSubmit = onSubmit,
                                modifier = Modifier,
                                enabled = { isSubmitEnabled },
                            )
                        }
                    }
                },
                title = {
                    val count = attendeesSearchState.attendees.size
                    Text(text = pluralStringResource(R.plurals.attendeesCount, count, count))
                },
            )
        },
        attendeesSearchState = attendeesSearchState,
        modifier = modifier,
        isSearchable = config.canEdit,
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
            config = EditAttendeesConfig(
                occurrenceId = OccurrenceId.Master(EventId("preview")),
                canEdit = true,
                isRecurring = false,
                calendarId = "preview",
            ),
            attendeesSearchState = rememberSaveableAttendeesSearchState(
                participants = remember { mutableStateSetOf<ParticipantUi>().apply { addAll(previewAttendees) } },
                results = remember { mutableStateListOf<AttendeesSearchResult>().apply { addAll(previewAttendees) } },
            ),
            goBack = {},
            onSubmit = {},
        )
    }
}
