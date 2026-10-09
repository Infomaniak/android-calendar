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
package com.infomaniak.calendar.ui.screen.eventDetail.attendeesSearch

import androidx.lifecycle.ViewModel
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(EventAttendeesViewModel::class)
class EventAttendeesViewModel : ViewModel() {
    private val _attendeesSearchState: MutableStateFlow<AttendeesSearchUiState> = MutableStateFlow(AttendeesSearchUiState.Loading)
    val attendeesSearchState: StateFlow<AttendeesSearchUiState> = _attendeesSearchState.asStateFlow()

    fun setAttendeesSearchState(attendeesSearchState: AttendeesSearchUiState) {
        _attendeesSearchState.value = attendeesSearchState
    }
}

sealed interface AttendeesSearchUiState {
    /** The parent screen hasn't provided its [AttendeesSearchState] yet. */
    data object Loading : AttendeesSearchUiState
    data class Success(val state: AttendeesSearchState) : AttendeesSearchUiState
}

