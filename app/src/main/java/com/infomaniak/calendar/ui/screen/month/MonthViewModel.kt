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
package com.infomaniak.calendar.ui.screen.month

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.components.month.model.MonthUi
import com.infomaniak.calendar.data.SettingsDataValues
import com.infomaniak.calendar.manager.SyncEventsManager
import com.infomaniak.calendar.manager.TimeZoneManager
import com.infomaniak.calendar.manager.VisibleMonthManager
import com.infomaniak.calendar.ui.screen.month.pendingKmp.buildMonths
import com.infomaniak.calendar.ui.screen.month.pendingKmp.toEventsByDate
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlin.time.Duration.Companion.seconds

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class MonthViewModel(
    private val calendarManager: CalendarManager,
    private val settingsDataValues: SettingsDataValues,
    visibleMonthManager: VisibleMonthManager,
    syncEventsManager: SyncEventsManager,
    timeZoneManager: TimeZoneManager,
) : ViewModel() {
    val isLoadingEvents: Flow<Boolean> = syncEventsManager.isLoadingEvents

    /** The loaded months, laid out with their events, around the visible one. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val months: StateFlow<Map<YearMonth, MonthUi>> = combine(visibleMonthManager.loadedMonths, timeZoneManager.timeZone, ::Pair)
        .flatMapLatest { (months, timeZone) -> observeMonths(months, timeZone) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeMonths(months: YearMonthRange, timeZone: TimeZone): Flow<Map<YearMonth, MonthUi>> {
        val daySlices = calendarManager.observeDaySlices(
            start = months.start.firstDay.atStartOfDayIn(timeZone),
            end = months.endInclusive.plus(1, DateTimeUnit.MONTH).firstDay.atStartOfDayIn(timeZone),
            timeZone = timeZone,
        )
        return combine(daySlices, settingsDataValues.firstDayOfWeek.flow, ::Pair).mapLatest { (slicesByDate, firstDayOfWeek) ->
            buildMonths(months, firstDayOfWeek, slicesByDate.toEventsByDate(timeZone))
        }
    }
}
