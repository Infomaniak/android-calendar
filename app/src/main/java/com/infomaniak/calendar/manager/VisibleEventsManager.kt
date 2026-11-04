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
package com.infomaniak.calendar.manager

import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventDaySlice
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Duration.Companion.seconds

/** Observes the events of the days around the visible date, shared by every calendar view. */
@SingleIn(AppScope::class)
@Inject
class VisibleEventsManager(calendarManager: CalendarManager, visibleDateManager: VisibleDateManager) {
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val timeZone = TimeZone.currentSystemDefault() // TODO: Adapt to user settings

    private val loadedDays = visibleDateManager.visibleDateFlow
        .scan(loadedDaysAround(visibleDateManager.visibleDate.value)) { loadedDays, visibleDate ->
            if (loadedDays.covers(neededDaysAround(visibleDate))) loadedDays else loadedDaysAround(visibleDate)
        }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val loadedEvents: StateFlow<LoadedEvents?> = loadedDays
        .flatMapLatest { days ->
            calendarManager.observeDaySlices(
                start = days.start.atStartOfDayIn(timeZone),
                end = days.endInclusive.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone),
                timeZone = timeZone,
            ).map { slicesByDay -> LoadedEvents(days, timeZone, slicesByDay) }
        }
        .stateIn(scope = managerScope, started = SharingStarted.WhileSubscribed(5.seconds), initialValue = null)

    private fun neededDaysAround(date: LocalDate): ClosedRange<LocalDate> {
        return date.minus(NEEDED_DAYS_BEFORE, DateTimeUnit.DAY)..date.plus(NEEDED_DAYS_AFTER, DateTimeUnit.DAY)
    }

    private fun loadedDaysAround(date: LocalDate): ClosedRange<LocalDate> {
        return date.minus(LOADED_DAYS_BEFORE, DateTimeUnit.DAY)..date.plus(LOADED_DAYS_AFTER, DateTimeUnit.DAY)
    }

    private fun ClosedRange<LocalDate>.covers(days: ClosedRange<LocalDate>): Boolean {
        return days.start >= start && days.endInclusive <= endInclusive
    }

    data class LoadedEvents(
        val days: ClosedRange<LocalDate>,
        val timeZone: TimeZone,
        val slicesByDay: Map<LocalDate, List<EventDaySlice>>,
    )

    companion object {
        // The week view needs the whole previous and next weeks, the planning the days below its top date.
        private const val NEEDED_DAYS_BEFORE = 14
        private const val NEEDED_DAYS_AFTER = 21
        private const val LOADED_DAYS_BEFORE = 28
        private const val LOADED_DAYS_AFTER = 42
    }
}
