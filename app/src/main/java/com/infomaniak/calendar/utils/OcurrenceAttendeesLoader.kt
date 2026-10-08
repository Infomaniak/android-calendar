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
package com.infomaniak.calendar.utils

import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

internal class OccurrenceAttendeesLoader(
    private val scope: CoroutineScope,
    private val savedStateHandle: SavedStateHandle,
    private val calendarManager: CalendarManager,
    private val state: AttendeesSearchState,
    private val preserveRestoredSelections: Boolean,
) {
    private var loadJob: Job? = null
    private var currentOccurrenceId: OccurrenceId? = null

    fun load(occurrenceId: OccurrenceId) {
        if (currentOccurrenceId == occurrenceId) return
        currentOccurrenceId = occurrenceId
        loadJob?.cancel()

        val ownsSavedState =
            savedStateHandle.get<String>(ATTENDEES_OCCURRENCE_KEY) ==
                    occurrenceId.value

        val preserveSelection = preserveRestoredSelections && ownsSavedState

        var initialAttendees = if (preserveSelection) {
            savedStateHandle
                .get<ArrayList<AttendeeUi>>(INITIAL_ATTENDEES_KEY)
                ?.toSet()
        } else {
            null
        }

        if (!preserveSelection) {
            savedStateHandle.remove<String>(ATTENDEES_OCCURRENCE_KEY)
            savedStateHandle.remove<ArrayList<AttendeeUi>>(INITIAL_ATTENDEES_KEY)
            state.reset()
        }

        loadJob = scope.launch {
            calendarManager.observeOccurrence(occurrenceId).collect { event ->
                ensureActive()
                if (currentOccurrenceId != occurrenceId) return@collect
                if (event == null) return@collect // Unavailable event is handled by the UI.

                val invited = event.attendees.map { it.toAttendeeUi() }
                val previousInitial = initialAttendees

                val canRefresh = if (previousInitial != null) {
                    state.hasSameAttendees(previousInitial)
                } else {
                    !preserveSelection
                }

                if (canRefresh) {
                    Snapshot.withMutableSnapshot {
                        state.attendees.clear()
                        state.attendees.addAll(invited)

                        // Don't replace active search results.
                        if (state.searchQueryTextFieldState.text.isBlank()) {
                            state.results.clear()
                            state.results.addAll(invited)
                        }
                    }

                    initialAttendees = invited.toSet()
                    savedStateHandle[INITIAL_ATTENDEES_KEY] = ArrayList(invited)
                } else if (previousInitial == null) {
                    // Older saved drafts may not contain a baseline.
                    initialAttendees = invited.toSet()
                    savedStateHandle[INITIAL_ATTENDEES_KEY] = ArrayList(invited)
                }

                savedStateHandle[ATTENDEES_OCCURRENCE_KEY] = occurrenceId.value
            }
        }
    }

    private companion object {
        const val ATTENDEES_OCCURRENCE_KEY = "attendeesOccurrenceId"
        const val INITIAL_ATTENDEES_KEY = "initialAttendees"
    }
}
