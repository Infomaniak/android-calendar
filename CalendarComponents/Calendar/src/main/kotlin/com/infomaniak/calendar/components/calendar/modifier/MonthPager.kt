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
import com.kizitonwose.calendar.compose.CalendarState
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

@Composable
internal fun rememberMonthPager(state: CalendarState, monthMargin: Int): CalendarPager {
    return remember(state, monthMargin) { MonthPager(state, monthMargin) }
}

private class MonthPager(
    private val state: CalendarState,
    override val monthMargin: Int,
) : CalendarPager {

    override val scrollableState get() = state
    override val displayedPage get() = state.firstVisibleMonth.yearMonth.firstDay
    override val pageRange get() = state.startMonth.firstDay..state.endMonth.firstDay

    override fun pageOf(date: LocalDate) = date.yearMonth.firstDay

    override fun pageAt(from: LocalDate, pageOffset: Int) = from.plus(pageOffset, DateTimeUnit.MONTH)

    override fun growRangeStart(page: LocalDate) {
        state.startMonth = page.yearMonth
    }

    override fun growRangeEnd(page: LocalDate) {
        state.endMonth = page.yearMonth
    }

    override suspend fun scrollToPage(page: LocalDate, animate: Boolean) {
        if (animate) state.animateScrollToMonth(page.yearMonth) else state.scrollToMonth(page.yearMonth)
    }
}
