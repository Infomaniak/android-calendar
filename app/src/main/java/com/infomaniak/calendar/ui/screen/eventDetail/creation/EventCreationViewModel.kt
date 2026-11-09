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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.manager.BackgroundEventComposer
import com.infomaniak.calendar.ui.screen.eventDetail.EventCreationCalendarsUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class EventCreationViewModel(
    eventCreationCalendarsUseCase: EventCreationCalendarsUseCase,
    private val backgroundEventComposer: BackgroundEventComposer,
) : ViewModel() {
    val uiState: StateFlow<EventCreationUiState> = eventCreationCalendarsUseCase.creationCalendars
        .map { calendars -> calendars?.let(EventCreationUiState::Success) ?: EventCreationUiState.NoWritableCalendar }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), EventCreationUiState.Loading)

    private var isSubmitted = false

    fun submitEvent(eventDraft: EventDraft): Boolean {
        if (isSubmitted) return false
        isSubmitted = true

        backgroundEventComposer.createEvent(eventDraft)

        return true
    }
}
