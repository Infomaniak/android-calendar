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
package com.infomaniak.calendar.components.calendar.modifier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.infomaniak.calendar.components.foundation.utils.startOfWeek
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private const val DAYS_IN_WEEK = 7

@Composable
internal fun rememberWeekPager(
    state: WeekCalendarState,
    firstDayOfWeek: DayOfWeek,
    monthMargin: Int,
): CalendarPager = remember(state, firstDayOfWeek, monthMargin) { WeekPager(state, firstDayOfWeek, monthMargin) }

private class WeekPager(
    private val state: WeekCalendarState,
    private val firstDayOfWeek: DayOfWeek,
    override val monthMargin: Int,
) : CalendarPager {
    override val scrollableState get() = state
    override val displayedPage get() = state.firstVisibleWeek.days.first().date
    override val pageRange get() = state.startDate..state.endDate

    override fun pageOf(date: LocalDate) = date.startOfWeek(firstDayOfWeek)

    override fun pageAt(from: LocalDate, pageOffset: Int) = from.plus(pageOffset * DAYS_IN_WEEK, DateTimeUnit.DAY)

    override fun growRangeStart(page: LocalDate) {
        state.startDate = page
    }

    override fun growRangeEnd(page: LocalDate) {
        state.endDate = page
    }

    override suspend fun scrollToPage(page: LocalDate, animate: Boolean) {
        if (animate) state.animateScrollToWeek(page) else state.scrollToWeek(page)
    }
}
