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
package com.infomaniak.calendar.components.day.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/** [dateRange] cut, from its start, into pages of [daysPerPage] consecutive days. */
@Immutable
internal data class DayPages(val dateRange: ClosedRange<LocalDate>, val daysPerPage: Int) {

    val count: Int = (dateRange.start.daysUntil(dateRange.endInclusive) + daysPerPage) / daysPerPage

    fun pageOf(date: LocalDate): Int = (dateRange.start.daysUntil(date) / daysPerPage).coerceIn(0, count - 1)

    fun firstDateOf(page: Int): LocalDate = dateRange.start.plus(page * daysPerPage, DateTimeUnit.DAY)

    fun datesOf(page: Int): List<LocalDate> {
        val firstDate = firstDateOf(page)
        return List(daysPerPage) { firstDate.plus(it, DateTimeUnit.DAY) }
    }
}
