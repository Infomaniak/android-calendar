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
package com.infomaniak.calendar.components.foundation.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.runtime.toMutableStateList
import com.infomaniak.calendar.components.foundation.models.AttendeeUi

@Stable
class AttendeesSearchState(
    val searchQueryTextFieldState: TextFieldState,
    val results: SnapshotStateList<AttendeeUi>,
    val attendees: SnapshotStateSet<AttendeeUi>,
    private val attendeesComparator: () -> Boolean,
) {
    companion object {
        fun saver(
            attendeesComparator: () -> Boolean,
        ): Saver<AttendeesSearchState, Any> = Saver(
            save = { state ->
                val savedQuery = with(TextFieldState.Saver) { save(state.searchQueryTextFieldState) }
                listOf(savedQuery, ArrayList(state.results), ArrayList(state.attendees))
            },
            restore = restore@{ saved ->
                val values = saved as? List<*> ?: return@restore null
                if (values.size != 3) return@restore null

                val query = values[0]
                    ?.let(TextFieldState.Saver::restore)
                    ?: return@restore null

                val results = (values[1] as? List<*>)
                    ?.map { it as? AttendeeUi ?: return@restore null }
                    ?: return@restore null

                val attendees = (values[2] as? List<*>)
                    ?.map { it as? AttendeeUi ?: return@restore null }
                    ?: return@restore null

                AttendeesSearchState(
                    searchQueryTextFieldState = query,
                    results = results.toMutableStateList(),
                    attendees = mutableStateSetOf<AttendeeUi>().apply {
                        addAll(attendees)
                    },
                    attendeesComparator = attendeesComparator,
                )
            },
        )
    }

    fun updateSearchQuery(query: String) {
        searchQueryTextFieldState.setTextAndPlaceCursorAtEnd(query)
    }

    fun reset(invitedAttendees: List<AttendeeUi> = emptyList()) {
        Snapshot.withMutableSnapshot {
            updateSearchQuery("")
            results.clear()
            results.addAll(invitedAttendees)
            attendees.clear()
            attendees.addAll(invitedAttendees)
        }
    }
}

/**
 * Remembers saveable attendee-search state initialized from attendees, results, and query.
 *
 * Mutations to its snapshot collections update consumers and are restored after state recreation.
 */
@Composable
fun rememberSaveableAttendeesSearchState(
    attendees: SnapshotStateSet<AttendeeUi>,
    results: List<AttendeeUi> = emptyList(),
    searchQuery: String = "",
    attendeesComparator: () -> Boolean = { true },
): AttendeesSearchState {
    val currentComparator by rememberUpdatedState(attendeesComparator)
    val saver = remember {
        AttendeesSearchState.saver {
            currentComparator()
        }
    }

    return rememberSaveable(saver = saver) {
        AttendeesSearchState(
            searchQueryTextFieldState = TextFieldState(initialText = searchQuery),
            results = results.toMutableStateList(),
            attendees = mutableStateSetOf<AttendeeUi>().apply {
                addAll(attendees)
            },
            attendeesComparator = { currentComparator() },
        )
    }
}
