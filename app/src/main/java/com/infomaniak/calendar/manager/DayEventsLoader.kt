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

import com.infomaniak.calendar.ui.model.TimelineUiState
import com.infomaniak.calendar.utils.toDayEventsByDate
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Duration.Companion.seconds

/** Observes the events the day, 3 days and week views show, sorted per day into all-day and timed events. */
@SingleIn(AppScope::class)
@Inject
class DayEventsLoader(private val calendarManager: CalendarManager, visibleDateManager: VisibleDateManager) {
    private val useCaseScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val dateRange = visibleDateManager.visibleDateFlow.map(::dateRangeAround)
    private val timeZone = flowOf(TimeZone.currentSystemDefault()) // TODO: Adapt to user settings

    @OptIn(ExperimentalCoroutinesApi::class)
    val observeEventsByDate: StateFlow<TimelineUiState> = combine(dateRange, timeZone, ::Pair)
        .flatMapLatest { (dateRange, timeZone) ->
            calendarManager.observeDaySlices(
                start = dateRange.start.atStartOfDayIn(timeZone),
                end = dateRange.endInclusive.atStartOfDayIn(timeZone),
                timeZone = timeZone,
            ).mapLatest { slices ->
                TimelineUiState.Success(slices.toDayEventsByDate(timeZone))
            }
        }
        .stateIn(
            scope = useCaseScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = TimelineUiState.Loading,
        )

    companion object {
        // Used by the day, 3 days and week views. For now, only load what's needed for the week view and reused it for the day and 3 days as well.
        private const val DAY_AGO = 7
        private const val DAY_AFTER = 14

        /** The days around [today] whose events are loaded. */
        fun dateRangeAround(today: LocalDate): ClosedRange<LocalDate> {
            return today.minus(DAY_AGO, DateTimeUnit.DAY)..today.plus(DAY_AFTER, DateTimeUnit.DAY)
        }
    }
}
