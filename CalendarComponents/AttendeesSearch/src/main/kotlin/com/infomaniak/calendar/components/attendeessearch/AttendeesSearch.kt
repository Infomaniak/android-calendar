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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.attendeessearch.component.AttendeesList
import com.infomaniak.calendar.components.attendeessearch.component.EmptyState
import com.infomaniak.calendar.components.attendeessearch.component.SearchBar
import com.infomaniak.calendar.components.attendeessearch.state.rememberSaveableAttendeesSearchState
import com.infomaniak.calendar.components.foundation.preview.previewAttendees
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
fun AttendeesSearch(
    attendeesSearchState: AttendeesSearchState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SearchBar(
            searchState = attendeesSearchState.searchQueryTextFieldState,
        )
        if (attendeesSearchState.searchResults.isEmpty()) {
            EmptyState(text = stringResource(R.string.attendeesEmptyState))
        } else {
            AttendeesList(
                attendees = { attendeesSearchState.searchResults },
                modifier = Modifier.padding(horizontal = EsdsTheme.spacing.md, vertical = EsdsTheme.spacing.sm),
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
                    attendees = previewAttendees,
                    contacts = previewAttendees,
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
                    attendees = listOf(),
                    contacts = listOf(),
                ),
            )
        }
    }
}
