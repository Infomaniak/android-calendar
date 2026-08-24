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
package com.infomaniak.calendar.ui.screen.day

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.data.CalendarDataValues
import com.infomaniak.calendar.manager.SyncEventsManager
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.core.common.utils.today
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Clock

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class DayViewModel(
    accountUtils: AccountUtils,
    calendarManager: CalendarManager,
    syncEventsManager: SyncEventsManager,
    private val calendarDataValues: CalendarDataValues,
) : ViewModel() {
    val isLoadingEvents: Flow<Boolean> = syncEventsManager.isLoadingEvents

    val hourHeight: Flow<Dp> = calendarDataValues.dayViewHourHeight.flow.map { it.dp }

    suspend fun saveHourHeight(hourHeight: Dp) = calendarDataValues.dayViewHourHeight.setValue(hourHeight.value)

    private val timeZone = TimeZone.currentSystemDefault()
    private val today = Clock.today(timeZone)

    val dateRange: ClosedRange<LocalDate> =
        today.minus(DAY_RANGE_DAYS, DateTimeUnit.DAY)..today.plus(DAY_RANGE_DAYS, DateTimeUnit.DAY)

    private val startDate = dateRange.start.atStartOfDayIn(timeZone)
    private val endDate = dateRange.endInclusive.atStartOfDayIn(timeZone)

    private val emailsByUserId = accountUtils.emailsByUserId.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val dayUiState: StateFlow<DayUiState> = calendarManager
        .observeDaySlices(startDate, endDate, timeZone)
        .mapLatest {
            val eventsByDate = it.toDayEventsByDate(emailsByUserId.first(), timeZone)
            DayUiState.Success({ eventsByDate })
        }
        .stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = DayUiState.Loading)

    companion object {
        const val DAY_RANGE_DAYS = 250
    }
}
