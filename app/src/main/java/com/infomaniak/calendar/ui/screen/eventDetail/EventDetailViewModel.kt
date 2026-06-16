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
package com.infomaniak.calendar.ui.screen.eventDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.ui.screen.eventDetail.utils.toDetailUiState
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@AssistedInject
class EventDetailViewModel(
    @Assisted private val eventId: EventId,
    private val calendarManager: CalendarManager,
) : ViewModel() {

    val uiState: StateFlow<EventDetailUiState>
        field = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)

    private val _events = Channel<EventDetailUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var observeJob: Job? = observeEvent(eventId)

    fun processAction(action: EventDetailAction) = when (action) {
        is EventDetailAction.OnClickDelete -> onClickDelete()
        is EventDetailAction.OnClickBack -> onClickBack()
    }

    private fun observeEvent(eventId: EventId) = viewModelScope.launch {
        calendarManager.observeEvent(eventId)
            .catch { uiState.value = EventDetailUiState.Error(it.message ?: "Unknown error") }
            .collect { event ->
                uiState.value = event?.toDetailUiState()
                    ?: EventDetailUiState.Error("Événement introuvable")
            }
    }

    private fun onClickDelete() {
        val current = uiState.value as? EventDetailUiState.Loaded ?: return
        observeJob?.cancel()
        observeJob = null
        viewModelScope.launch {
            runCatching {
                calendarManager.deleteEvent(current.eventId)
            }.onSuccess {
                _events.send(EventDetailUiEvent.NavigateBack)
            }.onFailure {
                uiState.value = EventDetailUiState.Error(it.message ?: "Unknown error")
            }
        }
    }

    private fun onClickBack() = viewModelScope.launch {
        _events.send(EventDetailUiEvent.NavigateBack)
    }

    @AssistedFactory
    fun interface Factory {
        fun create(eventId: EventId): EventDetailViewModel
    }
}
