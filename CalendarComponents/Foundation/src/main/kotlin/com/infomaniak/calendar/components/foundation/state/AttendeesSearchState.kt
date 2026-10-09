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
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import com.infomaniak.calendar.components.foundation.models.AttendeeUi

@Stable
class AttendeesSearchState(
    val searchQueryTextFieldState: TextFieldState,
    val results: SnapshotStateList<AttendeeUi>,
    val attendees: SnapshotStateSet<AttendeeUi>,
) {
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

    object Saver : androidx.compose.runtime.saveable.Saver<AttendeesSearchState, List<Any>> {
        private val resultsSaver = autoSaver<SnapshotStateList<AttendeeUi>>()
        private val attendeesSaver = autoSaver<SnapshotStateSet<AttendeeUi>>()

        override fun SaverScope.save(value: AttendeesSearchState): List<Any> = listOf(
            with(TextFieldState.Saver) { save(value.searchQueryTextFieldState)!! },
            with(resultsSaver) { save(value.results)!! },
            with(attendeesSaver) { save(value.attendees)!! },
        )

        override fun restore(value: List<Any>): AttendeesSearchState = AttendeesSearchState(
            searchQueryTextFieldState = TextFieldState.Saver.restore(value[0])!!,
            results = resultsSaver.restore(value[1])!!,
            attendees = attendeesSaver.restore(value[2])!!,
        )
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
    results: SnapshotStateList<AttendeeUi> ,
    searchQuery: String = "",
): AttendeesSearchState = rememberSaveable(saver = AttendeesSearchState.Saver) {
    AttendeesSearchState(
        searchQueryTextFieldState = TextFieldState(initialText = searchQuery),
        results = results,
        attendees = attendees,
    )
}
