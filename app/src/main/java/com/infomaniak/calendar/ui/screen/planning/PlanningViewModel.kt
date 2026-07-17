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
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.di.ViewModelKey
import com.infomaniak.calendar.manager.SyncEventsManager
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.core.common.utils.today
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Clock

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class PlanningViewModel(
    accountUtils: AccountUtils,
    calendarManager: CalendarManager,
    syncEventsManager: SyncEventsManager,
) : ViewModel() {
    val isLoadingEvents: Flow<Boolean> = syncEventsManager.isLoadingEvents

    private val timeZone = TimeZone.currentSystemDefault()
    val today = Clock.today(timeZone)

    private val startDate = today.minus(PLANNING_RANGE_DAYS, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
    private val endDate = today.plus(PLANNING_RANGE_DAYS, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

    private val emailsByUserId = accountUtils.emailsByUserId.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val eventsByWeekAndDay: Flow<EventsByWeekAndDay> = calendarManager
        .observeDaySlices(startDate, endDate, timeZone)
        .mapLatest { it.groupByWeekAndDay(emailsByUserId.first()) }
        .shareIn(scope = viewModelScope, started = SharingStarted.Lazily, replay = 1)

    val planningUiState: StateFlow<PlanningUiState> = eventsByWeekAndDay
        .map { events -> PlanningUiState.Success({ events }) }
        .stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = PlanningUiState.Loading)

    val nextEvents: StateFlow<List<EventUi.Normal>> = eventsByWeekAndDay
        .map { it.findNextEvents(Clock.System.now()) }
        .stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList())

    companion object {
        private const val PLANNING_RANGE_DAYS = 250
    }
}
