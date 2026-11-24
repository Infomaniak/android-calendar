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
package com.infomaniak.calendar.ui.screen.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.manager.SyncEventsManager
import com.infomaniak.calendar.manager.VisibleEventsManager
import com.infomaniak.calendar.manager.VisibleMonthManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class PlanningViewModel(
    syncEventsManager: SyncEventsManager,
    visibleEventsManager: VisibleEventsManager,
    visibleMonthManager: VisibleMonthManager,
) : ViewModel() {
    val isLoadingEvents: Flow<Boolean> = syncEventsManager.isLoadingEvents

    @OptIn(ExperimentalCoroutinesApi::class)
    val planningUiState: StateFlow<PlanningUiState> = visibleEventsManager.loadedEvents
        .filterNotNull()
        .mapLatest { loadedEvents ->
            PlanningUiState.Success(loadedEvents.slicesByDay.groupByDay(loadedEvents.days, loadedEvents.timeZone))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = PlanningUiState.Loading,
        )

    val eventDots = visibleMonthManager.eventDots
        .stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyMap())
}
