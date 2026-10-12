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
package com.infomaniak.calendar.ui.screen.threeDays

import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.data.CalendarDataValues
import com.infomaniak.calendar.manager.DayEventsLoader
import com.infomaniak.calendar.manager.SyncEventsManager
import com.infomaniak.calendar.manager.VisibleMonthManager
import com.infomaniak.calendar.ui.screen.day.DayEventsByDate
import com.infomaniak.core.common.utils.today
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class ThreeDayViewModel(
    dayEventsLoader: DayEventsLoader,
    syncEventsManager: SyncEventsManager,
    visibleMonthManager: VisibleMonthManager,
    private val calendarDataValues: CalendarDataValues,
) : ViewModel() {
    val isLoadingEvents: Flow<Boolean> = syncEventsManager.isLoadingEvents

    private val timeZone = TimeZone.currentSystemDefault()

    // TODO: Only load the days around the visible ones
    val dateRange: ClosedRange<LocalDate> = dayEventsLoader.dateRangeAround(Clock.today(timeZone))

    val eventsByDate: StateFlow<DayEventsByDate> = dayEventsLoader.observeEventsByDate(dateRange, timeZone)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = emptyMap())

    val eventDots = visibleMonthManager.eventDots
        .stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyMap())

    val hourHeight: Flow<Dp> = calendarDataValues.dayViewHourHeight.flow

    suspend fun saveHourHeight(hourHeight: Dp) = calendarDataValues.dayViewHourHeight.setValue(hourHeight)
}
