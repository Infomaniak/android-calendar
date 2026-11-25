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
package com.infomaniak.calendar.ui.screen.month.pendingKmp

import com.infomaniak.calendar.components.foundation.utils.startOfWeek
import com.infomaniak.calendar.components.month.model.MonthUi
import com.infomaniak.calendar.ui.model.OccurrenceEventSummaryUi
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus

/**
 * Every month of [months] laid out with the events of [eventsByDate], each day's in display order.
 *
 * TODO[kmpMonth]: The whole `pendingKmp` package goes once KMP returns months with their packed weeks; only the
 *  source of `MonthViewModel.months` changes then.
 */
internal fun buildMonths(
    months: Iterable<YearMonth>,
    firstDayOfWeek: DayOfWeek,
    eventsByDate: Map<LocalDate, List<OccurrenceEventSummaryUi>>,
): Map<YearMonth, MonthUi> = months.associateWith { buildMonth(it, firstDayOfWeek, eventsByDate) }

/** [month] as the weeks it touches, from the one holding its first day to the one holding its last, events packed. */
internal fun buildMonth(
    month: YearMonth,
    firstDayOfWeek: DayOfWeek,
    eventsByDate: Map<LocalDate, List<OccurrenceEventSummaryUi>>,
): MonthUi = MonthUi(month, weeks = weeksOf(month, firstDayOfWeek).map { dates -> packWeek(dates, month, eventsByDate) })

private fun weeksOf(month: YearMonth, firstDayOfWeek: DayOfWeek): List<List<LocalDate>> {
    return generateSequence(month.firstDay.startOfWeek(firstDayOfWeek)) { it.plus(1, DateTimeUnit.WEEK) }
        .takeWhile { it <= month.lastDay }
        .map { weekStart -> List(DAYS_IN_WEEK) { weekStart.plus(it, DateTimeUnit.DAY) } }
        .toList()
}

private const val DAYS_IN_WEEK = 7
