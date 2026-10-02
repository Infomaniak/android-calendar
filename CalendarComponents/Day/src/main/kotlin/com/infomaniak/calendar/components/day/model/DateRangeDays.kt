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

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/** A range of dates seen as a row of days, indexed from its start. */

internal val ClosedRange<LocalDate>.dayCount: Int get() = start.daysUntil(endInclusive) + 1

internal fun ClosedRange<LocalDate>.indexOf(date: LocalDate): Int = start.daysUntil(date)

internal fun ClosedRange<LocalDate>.dateAt(index: Int): LocalDate = start.plus(index, DateTimeUnit.DAY)
