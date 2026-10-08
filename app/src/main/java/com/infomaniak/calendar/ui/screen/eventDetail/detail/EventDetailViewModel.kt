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
package com.infomaniak.calendar.ui.screen.eventDetail.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.ui.screen.eventDetail.GetEventDetailUiUseCase
import com.infomaniak.calendar.utils.attendeesSearchState
import com.infomaniak.calendar.utils.toAttendeeUi
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AssistedInject
class EventDetailViewModel(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val calendarManager: CalendarManager,
    private val getEventDetailUiUseCase: GetEventDetailUiUseCase,
) : ViewModel() {
    val eventDetailUi = getEventDetailUiUseCase.eventDetailUi
    val attendeesSearchState: AttendeesSearchState = savedStateHandle.attendeesSearchState()

    private var attendeesLoadJob: Job? = null
    private var currentOccurrenceId: OccurrenceId? = null

    fun setOccurrenceId(occurrenceId: OccurrenceId) {
        getEventDetailUiUseCase.setOccurrenceId(occurrenceId)

        // it shouldn't reload the attendees if the occurrence ID hasn't changed
        if (currentOccurrenceId == occurrenceId) return
        currentOccurrenceId = occurrenceId
        attendeesLoadJob = viewModelScope.launch {
            val event = calendarManager.observeOccurrence(occurrenceId).first()
                ?: return@launch

            attendeesSearchState.reset(
                invitedAttendees = event.attendees.map { it.toAttendeeUi() },
            )
        }
    }
    
    @AssistedFactory
    @ViewModelAssistedFactoryKey(EventDetailViewModel::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): EventDetailViewModel = create(extras.createSavedStateHandle())

        fun create(@Assisted savedStateHandle: SavedStateHandle): EventDetailViewModel
    }
}

private const val ATTENDEES_LOADED_KEY = "attendeesLoaded"
