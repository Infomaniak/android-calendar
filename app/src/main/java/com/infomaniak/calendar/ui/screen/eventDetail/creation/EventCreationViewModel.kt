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
import com.infomaniak.calendar.components.eventdetail.form.EventDraft
import com.infomaniak.calendar.ui.screen.eventDetail.model.EventFormCalendars
import com.infomaniak.calendar.ui.screen.eventDetail.EventFormCalendarsUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(EventCreationViewModel::class)
class EventCreationViewModel(eventFormCalendarsUseCase: EventFormCalendarsUseCase) : ViewModel() {
    val uiState = flowOf(Unit).stateIn(viewModelScope, SharingStarted.Lazily, Unit)

    val eventFormCalendars: StateFlow<EventFormCalendars?> = eventFormCalendarsUseCase.creationCalendars

    fun submitEvent(eventDraft: EventDraft) {
        // TODO
    }
}
