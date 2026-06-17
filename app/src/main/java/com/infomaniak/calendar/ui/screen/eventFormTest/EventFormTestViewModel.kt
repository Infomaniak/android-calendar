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
package com.infomaniak.calendar.ui.screen.eventFormTest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.ui.screen.eventFormTest.model.CalendarChoice
import com.infomaniak.calendar.ui.screen.eventFormTest.model.EventFormData
import com.infomaniak.calendar.ui.screen.eventFormTest.utils.toChoice
import com.infomaniak.calendar.ui.screen.eventFormTest.utils.toEditData
import com.infomaniak.calendar.ui.screen.eventFormTest.utils.toFormData
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@AssistedInject
class EventFormTestViewModel(
    @Assisted private val eventId: EventId?,
    private val calendarManager: CalendarManager,
) : ViewModel() {
    val uiState: StateFlow<EventFormUiState>
        field = MutableStateFlow<EventFormUiState>(EventFormUiState.Loading)
    private val _events = Channel<EventFormUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun processAction(action: EventFormAction) = when (action) {
        is EventFormAction.OnTitleChange -> updateForm { it.copy(title = action.title) }
        is EventFormAction.OnAllDayChange -> updateForm { it.copy(isAllDay = action.isAllDay) }
        is EventFormAction.OnStartChange -> updateForm { it.copy(start = action.start) }
        is EventFormAction.OnEndChange -> updateForm { it.copy(end = action.end) }
        is EventFormAction.OnLocationChange -> updateForm { it.copy(location = action.location) }
        is EventFormAction.OnDescriptionChange -> updateForm { it.copy(description = action.description) }
        is EventFormAction.OnCalendarChange -> updateForm { it.copy(calendarId = action.calendarId) }
        is EventFormAction.OnClickSave -> onClickSave()
        is EventFormAction.OnClickBack -> onClickBack()
    }

    private fun load() = viewModelScope.launch {
        runCatching {
            val calendars = calendarManager.observeCalendars().first().map { it.toChoice() }
            val form = if (eventId != null) {
                calendarManager.observeEvent(eventId).filterNotNull().first().toFormData()
            } else {
                defaultForm(calendars)
            }
            EventFormUiState.Editing(form = form, calendars = calendars)
        }.onSuccess { uiState.value = it }
            .onFailure { uiState.value = EventFormUiState.Error(it.message ?: "Unknown error") }
    }

    private fun onClickSave() {
        val state = uiState.value as? EventFormUiState.Editing ?: return
        // TODO: creation (eventId == null) is wired once CalendarManager.createEvent lands.
        val eventId = eventId ?: return
        uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            runCatching {
                calendarManager.updateEvent(eventId, state.form.toEditData())
            }.onSuccess {
                _events.send(EventFormUiEvent.NavigateBack)
            }.onFailure {
                uiState.value = EventFormUiState.Error(it.message ?: "Unknown error")
            }
        }
    }

    private fun onClickBack() = viewModelScope.launch {
        _events.send(EventFormUiEvent.NavigateBack)
    }

    private fun updateForm(transform: (EventFormData) -> EventFormData) {
        val state = uiState.value as? EventFormUiState.Editing ?: return
        uiState.value = state.copy(form = transform(state.form))
    }

    private fun defaultForm(calendars: List<CalendarChoice>): EventFormData {
        val calendarId = calendars.firstOrNull()?.id ?: error("No calendar available")
        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        val start = LocalDateTime(today, LocalTime(9, 0))
        return EventFormData(
            title = "",
            isAllDay = false,
            start = start,
            end = LocalDateTime(today, LocalTime(10, 0)),
            location = "",
            description = "",
            calendarId = calendarId,
        )
    }
    @AssistedFactory
    fun interface Factory {
        fun create(eventId: EventId?): EventFormTestViewModel
    }
}


