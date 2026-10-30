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
import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.ui.screen.eventDetail.EventComposerUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.screen.eventDetail.SaveEventResult
import com.infomaniak.calendar.ui.screen.eventDetail.scope.RecurrentEventScope
import com.infomaniak.calendar.utils.saveableAttendeesSearchState
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AssistedInject
class EventEditViewModel(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val eventComposerUseCase: EventComposerUseCase,
) : ViewModel() {
    val attendeesSearchState: AttendeesSearchState = savedStateHandle.saveableAttendeesSearchState()

    private var areAttendeesInitialized: Boolean
        get() = savedStateHandle[ARE_ATTENDEES_INITIALIZED_KEY] ?: false
        set(value) {
            savedStateHandle[ARE_ATTENDEES_INITIALIZED_KEY] = value
        }

    suspend fun initAttendeesSearchState(eventDetailUi: Flow<EventDetailUiState>) {
        if (areAttendeesInitialized) return
        val event = eventDetailUi.filterIsInstance<EventDetailUiState.Displayable.Success>().first()
        attendeesSearchState.attendees.addAll(event.eventDetail.attendees.all)
        areAttendeesInitialized = true
    }

    suspend fun submitEvent(occurrenceId: OccurrenceId, scope: RecurrentEventScope, eventDraft: EventDraft): SaveEventResult {
        val result = CompletableDeferred<SaveEventResult>()

        viewModelScope.launch {
            result.complete(eventComposerUseCase.updateEvent(occurrenceId, scope, eventDraft))
        }

        return result.await()
    }

    @AssistedFactory
    @ViewModelAssistedFactoryKey(EventEditViewModel::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): EventEditViewModel = create(extras.createSavedStateHandle())

        fun create(@Assisted savedStateHandle: SavedStateHandle): EventEditViewModel
    }

    companion object {
        private const val ARE_ATTENDEES_INITIALIZED_KEY = "areAttendeesInitialized"
    }
}
