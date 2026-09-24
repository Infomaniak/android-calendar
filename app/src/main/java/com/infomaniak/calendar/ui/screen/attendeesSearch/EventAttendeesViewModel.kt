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
package com.infomaniak.calendar.ui.screen.attendeesSearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.ui.screen.eventDetail.GetEventDetailUiUseCase
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Attendee
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class EventAttendeesViewModel(private val getEventDetailUiUseCase: GetEventDetailUiUseCase) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventAttendeesState: StateFlow<EventAttendeesUiState> = getEventDetailUiUseCase.eventFlow
        .map { event ->
            val attendees = event?.attendees.orEmpty()
            when {
                event == null -> EventAttendeesUiState.EventMissing
                else -> EventAttendeesUiState.Loaded(
                    attendees = attendees,
                    contacts = attendees,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, EventAttendeesUiState.Loading)

    fun setOccurrenceId(occurrenceId: OccurrenceId) = getEventDetailUiUseCase.setOccurrenceId(occurrenceId)
}

sealed interface EventAttendeesUiState {
    data object Loading : EventAttendeesUiState
    data object EventMissing : EventAttendeesUiState
    data class Loaded(val attendees: List<Attendee>, val contacts: List<Attendee>) : EventAttendeesUiState
}

