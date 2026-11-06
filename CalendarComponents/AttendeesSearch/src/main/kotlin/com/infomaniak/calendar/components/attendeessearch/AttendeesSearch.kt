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
package com.infomaniak.calendar.components.attendeessearch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.attendeessearch.component.AttendeesList
import com.infomaniak.calendar.components.foundation.component.EmptyState
import com.infomaniak.calendar.components.foundation.component.InformationBlock
import com.infomaniak.calendar.components.foundation.component.SearchBar
import com.infomaniak.calendar.components.foundation.models.AttendeesSearchResult
import com.infomaniak.calendar.components.foundation.models.ParticipantUi
import com.infomaniak.calendar.components.foundation.preview.previewAttendees
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.components.foundation.state.rememberSaveableAttendeesSearchState
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.UsersStacked

private const val MAX_ATTENDEE_COUNT = 100

@Composable
fun AttendeesSearch(
    attendeesSearchState: AttendeesSearchState<*>,
    onShowSnackbar: (message: String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    isSearchable: Boolean = true,
) {
    val isDisplayingSearchResults = attendeesSearchState.searchQueryTextFieldState.text.isNotBlank() && isSearchable

    val attendeesList: List<AttendeesSearchResult> = if (isDisplayingSearchResults) {
        attendeesSearchState.results
    } else {
        attendeesSearchState.attendees
    }

    val horizontalContentPadding = contentPadding.onlyHorizontal()

    Column(
        modifier = modifier
            .padding(horizontalContentPadding)
            .padding(top = contentPadding.calculateTopPadding() + EsdsTheme.spacing.xl)
            .padding(horizontal = EsdsTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xl),
    ) {
        if (isSearchable) {
            AnimatedVisibility(attendeesSearchState.attendees.size >= MAX_ATTENDEE_COUNT) {
                InformationBlock(stringResource(R.string.attendeesLimitReached))
            }

            SearchBar(
                searchState = attendeesSearchState.searchQueryTextFieldState,
                placeholder = stringResource(R.string.attendeesSearchPlaceholder),
            )
        }

        val bottomContentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding())
        if (attendeesSearchState.searchQueryTextFieldState.text.isBlank() && attendeesList.isEmpty()) {
            EmptyState(
                text = stringResource(R.string.attendeesEmptyStateTitle),
                description = stringResource(R.string.attendeesEmptyStateDescription),
                icon = EsdsIcons.UsersStacked,
                modifier = Modifier.padding(bottomContentPadding),
            )
        } else {
            val resources = LocalResources.current

            AttendeesList(
                results = { attendeesList },
                query = { attendeesSearchState.searchQueryTextFieldState.text.toString() },
                onAddCustomAttendee = {
                    val result = attendeesSearchState.addCustomAttendee()
                    result.messageRes?.let { onShowSnackbar(resources.getString(it)) }
                },
                onRemove = attendeesSearchState::removeAttendee,
                modifier = Modifier.padding(vertical = EsdsTheme.spacing.sm),
                onClick = if (isDisplayingSearchResults) {
                    { attendee ->
                        if (attendeesSearchState.attendees.size >= MAX_ATTENDEE_COUNT) {
                            onShowSnackbar(resources.getString(R.string.attendeesLimitReached))
                        } else {
                            attendeesSearchState.addAttendee(attendee)
                        }
                    }
                } else {
                    null
                },
                isEnabled = { attendee ->
                    !isDisplayingSearchResults || attendee.email.lowercase() !in attendeesSearchState.attendeesEmailsLowercase
                },
                enableDeletion = { isSearchable && isDisplayingSearchResults.not() },
                contentPadding = bottomContentPadding,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            AttendeesSearch(
                attendeesSearchState = rememberSaveableAttendeesSearchState(
                    participants = remember { mutableStateSetOf<ParticipantUi>().apply { addAll(previewAttendees) } },
                    results = remember { previewAttendees.toMutableStateList() },
                ),
                onShowSnackbar = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewEmptySearchState() {
    MaterialTheme {
        Surface {
            AttendeesSearch(
                attendeesSearchState = rememberSaveableAttendeesSearchState(
                    participants = remember { mutableStateSetOf() },
                    results = remember { mutableStateListOf() },
                ),
                onShowSnackbar = {},
            )
        }
    }
}

@Preview
@Composable
private fun MaxAttendeePreview() {
    val participants = remember {
        val attendee = previewAttendees.first()
        mutableStateSetOf<ParticipantUi>().apply {
            repeat(MAX_ATTENDEE_COUNT) { index ->
                add(attendee.copy(email = "attendee$index@example.com")) // Unique emails so LazyColumn keys (and the set) stay distinct
            }
        }
    }

    MaterialTheme {
        Surface {
            AttendeesSearch(
                attendeesSearchState = rememberSaveableAttendeesSearchState(
                    participants = participants,
                    results = remember { mutableStateListOf() },
                ),
                onShowSnackbar = {},
            )
        }
    }
}
