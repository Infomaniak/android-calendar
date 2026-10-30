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
package com.infomaniak.calendar.ui.screen.eventDetail.creation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.data.SettingsDataValues
import com.infomaniak.calendar.ui.screen.eventDetail.EventComposerUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.EventCreationCalendarsUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.SaveEventResult
import com.infomaniak.calendar.utils.saveableAttendeesSearchState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class EventCreationViewModel(
    @Assisted savedStateHandle: SavedStateHandle,
    eventCreationCalendarsUseCase: EventCreationCalendarsUseCase,
    settingsDataValues: SettingsDataValues,
    private val eventComposerUseCase: EventComposerUseCase,
) : ViewModel() {
    val uiState: StateFlow<EventCreationUiState> = eventCreationCalendarsUseCase.creationCalendars
        .map { calendars -> calendars?.let(EventCreationUiState::Success) ?: EventCreationUiState.NoWritableCalendar }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), EventCreationUiState.Loading)

    val attendeesSearchState: AttendeesSearchState = savedStateHandle.saveableAttendeesSearchState()

    val defaultEventDuration = settingsDataValues.defaultEventDuration.flow

    suspend fun submitEvent(eventDraft: EventDraft): SaveEventResult {
        val result = CompletableDeferred<SaveEventResult>()

        viewModelScope.launch {
            result.complete(eventComposerUseCase.createEvent(eventDraft))
        }

        return result.await()
    }

    @AssistedFactory
    @ViewModelAssistedFactoryKey(EventCreationViewModel::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): EventCreationViewModel = create(extras.createSavedStateHandle())

        fun create(@Assisted savedStateHandle: SavedStateHandle): EventCreationViewModel
    }
}
