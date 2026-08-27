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
package com.infomaniak.calendar.components.foundation.utils.timeFormatter

import androidx.compose.runtime.Composable
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import java.time.Month
import java.time.format.TextStyle
import kotlin.time.Clock

/** `May` */
@Composable
fun YearMonth.formatShortMonth(): String = javaMonth().getDisplayName(TextStyle.SHORT, currentLocale())

/** `May` or `May 2024`, gaining the year when the month leaves [currentYear]. */
@Composable
fun YearMonth.formatMonthAndOptionalYear(currentYear: Int = Clock.today().year): String {
    val locale = currentLocale()
    val monthName = javaMonth().getDisplayName(TextStyle.FULL_STANDALONE, locale).titlecaseFirstChar(locale)

    return if (year == currentYear) monthName else "$monthName $year"
}

private fun YearMonth.javaMonth(): Month = Month.of(month.number)
