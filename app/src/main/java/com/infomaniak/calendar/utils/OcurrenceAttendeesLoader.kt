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

import androidx.lifecycle.SavedStateHandle
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
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

        if (
            preserveRestoredSelections &&
            savedStateHandle.get<String>(ATTENDEES_OCCURRENCE_KEY) ==
            occurrenceId.value
        ) {
            return
        }

        savedStateHandle.remove<String>(ATTENDEES_OCCURRENCE_KEY)
        state.reset()

        loadJob = scope.launch {
            val event = calendarManager.observeOccurrence(occurrenceId).first()
                ?: return@launch // The screen handles unavailable events.

            ensureActive()
            if (currentOccurrenceId != occurrenceId) return@launch

            state.reset(
                invitedAttendees = event.attendees.map { it.toAttendeeUi() },
            )
            savedStateHandle[ATTENDEES_OCCURRENCE_KEY] = occurrenceId.value
        }
    }

    private companion object {
        const val ATTENDEES_OCCURRENCE_KEY = "attendeesOccurrenceId"
    }
}
