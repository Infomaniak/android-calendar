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
package com.infomaniak.calendar.components.attendeessearch.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.infomaniak.calendar.components.foundation.models.AttendeeUi

@Stable
class AttendeesState(
    private val attendees: List<AttendeeUi>,
    private val contacts: List<AttendeeUi>,
    private val searchQuery: MutableState<String>,
) {

    val searchResults by derivedStateOf {
        val query = searchQuery.value.trim()
        if (query.isEmpty()) {
            attendees
        } else {
            contacts.filter { it.matchesQuery(query) }
        }
    }

    fun updateQuery(query: String) {
        searchQuery.value = query
    }

    // This function will be replaced by KMP search
    private fun AttendeeUi.matchesQuery(query: String): Boolean {
        val normalizedQuery = query.lowercase()

        return email.lowercase().contains(normalizedQuery) || displayName?.lowercase()?.contains(normalizedQuery) == true
    }
}

@Composable
fun rememberSaveableAttendeesState(
    attendees: List<AttendeeUi>,
    contacts: List<AttendeeUi>,
    searchQuery: String = "",
): AttendeesState {
    val searchQueryState = rememberSaveable { mutableStateOf(searchQuery) }

    return remember(attendees, contacts, searchQueryState) {
        AttendeesState(attendees = attendees, contacts = contacts, searchQuery = searchQueryState)
    }
}
