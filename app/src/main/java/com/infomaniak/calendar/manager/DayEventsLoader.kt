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

import com.infomaniak.calendar.ui.screen.day.DayEventsByDate
import com.infomaniak.calendar.ui.screen.day.toDayEventsByDate
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Observes the events the day, 3 days and week views show, sorted per day into all-day and timed events. */
@Inject
class DayEventsLoader(private val calendarManager: CalendarManager) {
    /** The days around [today] whose events are loaded. */
    fun dateRangeAround(today: LocalDate): ClosedRange<LocalDate> {
        return today.minus(DAY_RANGE_DAYS, DateTimeUnit.DAY)..today.plus(DAY_RANGE_DAYS, DateTimeUnit.DAY)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeEventsByDate(
        dateRange: ClosedRange<LocalDate>,
        timeZone: TimeZone,
    ): Flow<DayEventsByDate> = calendarManager.observeDaySlices(
        start = dateRange.start.atStartOfDayIn(timeZone),
        end = dateRange.endInclusive.atStartOfDayIn(timeZone),
        timeZone = timeZone,
    ).mapLatest { slices ->
        slices.toDayEventsByDate(timeZone)
    }

    private companion object {
        const val DAY_RANGE_DAYS = 250
    }
}
