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
package com.infomaniak.calendar.components.planning

import androidx.compose.runtime.Immutable
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.models.YearWeek
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/** Gives every date a fixed item index: each week is its header followed by its seven days. */
@Immutable
class PlanningTimeline(private val weekNumbering: WeekNumbering) {
    private val origin = weekNumbering.weekOf(FIRST_DATE).firstDay

    val itemCount: Int = (origin.daysUntil(LAST_DATE) / DAYS_IN_WEEK + 1) * ITEMS_PER_WEEK

    fun indexOf(date: LocalDate): Int {
        val days = origin.daysUntil(date.coerceIn(origin, LAST_DATE))
        return days / DAYS_IN_WEEK * ITEMS_PER_WEEK + 1 + days % DAYS_IN_WEEK
    }

    /** The date of a day's item, or the first day of the week for a week header. */
    fun dateAt(index: Int): LocalDate {
        val dayOfWeek = (index % ITEMS_PER_WEEK - 1).coerceAtLeast(0)
        return origin.plus(index / ITEMS_PER_WEEK * DAYS_IN_WEEK + dayOfWeek, DateTimeUnit.DAY)
    }

    internal fun isWeekHeader(index: Int): Boolean = index % ITEMS_PER_WEEK == 0

    internal fun weekAt(index: Int): YearWeek = weekNumbering.weekOf(dateAt(index))

    private companion object {
        val FIRST_DATE = LocalDate(1900, 1, 1)
        val LAST_DATE = LocalDate(2199, 12, 31)
        const val DAYS_IN_WEEK = 7
        const val ITEMS_PER_WEEK = DAYS_IN_WEEK + 1
    }
}
