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
package com.infomaniak.calendar.ui.screen.eventDetail.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.screen.eventDetail.EventFormCalendarsUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.GetEventDetailUiUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.model.EventFormCalendars
import com.infomaniak.calendar.utils.attendeesSearchState
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AssistedInject
class EventEditViewModel(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val getEventDetailUiUseCase: GetEventDetailUiUseCase,
    eventFormCalendarsUseCase: EventFormCalendarsUseCase,
) : ViewModel() {
    val eventDetailUi = getEventDetailUiUseCase.eventDetailUi
    val attendeesSearchState: AttendeesSearchState = savedStateHandle.attendeesSearchState()
    val eventFormCalendars: StateFlow<EventFormCalendars?> = eventFormCalendarsUseCase
        .editionCalendars(getEventDetailUiUseCase.eventCalendar)

    init {
        viewModelScope.launch {
            val event = eventDetailUi.filterIsInstance<EventDetailUiState.Success>().first().eventDetail
            attendeesSearchState.attendees.addAll(event.attendees.all)
        }
    }

    fun setOccurrenceId(occurrenceId: OccurrenceId) {
        getEventDetailUiUseCase.setOccurrenceId(occurrenceId)
    }

    @AssistedFactory
    @ViewModelAssistedFactoryKey(EventEditViewModel::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): EventEditViewModel = create(extras.createSavedStateHandle())

        fun create(@Assisted savedStateHandle: SavedStateHandle): EventEditViewModel
    }
}
