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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import java.util.Locale

@Stable
class AttendeesState(
    private val attendees: List<AttendeeUi>,
    private val contacts: List<AttendeeUi>,
) {
    var searchQuery by mutableStateOf("")
        private set

    val searchResults by derivedStateOf {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            attendees
        } else {
            contacts.filter { it.matchesQuery(query) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
    }

    private fun AttendeeUi.matchesQuery(query: String): Boolean {
        val normalizedQuery = query.lowercase(Locale.ROOT)
        return sequenceOf(email, displayName)
            .filterNotNull()
            .any { value -> value.lowercase(Locale.ROOT).contains(normalizedQuery) }
    }
}

@Composable
fun rememberAttendeesState(
    attendees: List<AttendeeUi>,
    contacts: List<AttendeeUi>,
): AttendeesState = remember(attendees, contacts) {
    AttendeesState(attendees = attendees, contacts = contacts)
}
