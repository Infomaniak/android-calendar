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
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.calendar.utils.toAttendeeUi
import com.infomaniak.calendar.utils.toEventDetailUi
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Attendee
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class EventDetailViewModel(
    accountUtils: AccountUtils,
    private val calendarManager: CalendarManager,
) : ViewModel() {
    private val eventIdFlow: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val searchQueryFlow = MutableStateFlow("")

    val searchQuery: StateFlow<String> = searchQueryFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    private val eventFlow = eventIdFlow
        .distinctUntilChanged()
        .flatMapLatest { eventId -> calendarManager.observeEvent(EventId(eventId)) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventAttendeesState: StateFlow<EventAttendeesUiState> = eventFlow
        .map { event ->
            val attendees = event?.attendees.orEmpty().map(Attendee::toAttendeeUi)

            when {
                event == null -> EventAttendeesUiState.EventMissing
                else -> EventAttendeesUiState.Loaded(attendees)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, EventAttendeesUiState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventDetailUi = eventFlow
        .flatMapLatest { event ->
            if (event == null) {
                flowOf(null)
            } else {
                calendarManager
                    .observeCalendars()
                    .map { it.find { calendar -> calendar.id == event.calendarId } }
                    .map { calendar -> calendar?.let { event to it } }
            }
        }
        .combine(accountUtils.emailsByUserId) { eventAndCalendar, emailsByUserId ->
            val (event, calendar) = eventAndCalendar ?: return@combine EventDetailUiState.Deleted

            event
                .toEventDetailUi(calendar, emailsByUserId)
                .let(EventDetailUiState::Success)
        }

    fun setEventId(eventId: String) {
        eventIdFlow.tryEmit(eventId)
    }

    // TODO: add search contacts functionality
    fun onSearchQueryChanged(query: String) {
        searchQueryFlow.value = query
    }
}

sealed interface EventAttendeesUiState {
    data object Loading : EventAttendeesUiState
    data object EventMissing : EventAttendeesUiState
    data class Loaded(val attendees: List<AttendeeUi>) : EventAttendeesUiState
}
