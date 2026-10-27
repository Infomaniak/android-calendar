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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.attendeessearch.component.AttendeesList
import com.infomaniak.calendar.components.foundation.component.EmptyState
import com.infomaniak.calendar.components.foundation.component.SearchBar
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.preview.previewAttendees
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.components.foundation.state.rememberSaveableAttendeesSearchState
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.UserMagnifyingGlass

@Composable
fun AttendeesSearch(
    attendeesSearchState: AttendeesSearchState,
    modifier: Modifier = Modifier,
) {
    val isDisplayingSearchResults = attendeesSearchState.searchQueryTextFieldState.text.isNotBlank()

    val attendeesList: List<AttendeeUi> = if (isDisplayingSearchResults) {
        attendeesSearchState.results
    } else {
        attendeesSearchState.attendees.toList()
    }

    Column(modifier = modifier) {
        SearchBar(
            searchState = attendeesSearchState.searchQueryTextFieldState,
            placeholder = stringResource(R.string.searchForAttendees),
        )
        if (attendeesList.isEmpty()) {
            EmptyState(text = stringResource(R.string.attendeesEmptyState), icon = EsdsIcons.UserMagnifyingGlass)
        } else {
            AttendeesList(
                attendees = { attendeesList },
                modifier = Modifier.padding(horizontal = EsdsTheme.spacing.md, vertical = EsdsTheme.spacing.sm),
                onClick = if (isDisplayingSearchResults) {
                    { attendee -> attendeesSearchState.addAttendee(attendee) }
                } else {
                    null
                },
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
                    attendees = remember { mutableStateSetOf<AttendeeUi>().apply { addAll(previewAttendees) } },
                    results = remember { previewAttendees.toMutableStateList() },
                ),
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
                    attendees = remember { mutableStateSetOf() },
                    results = remember { mutableStateListOf() },
                ),
            )
        }
    }
}
