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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaLocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Clock

private const val FULL_DATE_SKELETON = "EEEEdMMMM"
private const val FULL_DATE_WITH_YEAR_SKELETON = "yEEEEdMMMM"

private const val SHORT_DAY_NAME_PATTERN = "EEE"
private const val DAY_WITH_SHORT_MONTH_PATTERN = "EEEE - d MMM"

//region Exposed formatting methods
/** `W` */
@Composable
fun DayOfWeek.formatNarrowDayName(): String = toJavaDayOfWeek().getDisplayName(TextStyle.NARROW_STANDALONE, currentLocale())

/** `Wed` */
@Composable
fun LocalDate.formatShortDayName(): String = toJavaLocalDate().format(fixedFormatter(SHORT_DAY_NAME_PATTERN, currentLocale()))

/** `Wednesday - 20 May`, keeping the day view header's own layout in every language. */
@Composable
fun LocalDate.formatDayWithShortMonth(): String {
    val locale = currentLocale()

    return toJavaLocalDate().format(fixedFormatter(DAY_WITH_SHORT_MONTH_PATTERN, locale)).titlecaseFirstChar(locale)
}

/** `Wednesday, May 20, 2026`, for labels that have to stand on their own such as content descriptions. */
@Composable
fun LocalDate.formatFullDateWithYear(): String = format(FULL_DATE_WITH_YEAR_SKELETON, currentLocale())
//endregion

//region Underlying testable logic
internal fun LocalDate.formatFullDate(locale: Locale, currentYear: Int): String {
    return format(if (year == currentYear) FULL_DATE_SKELETON else FULL_DATE_WITH_YEAR_SKELETON, locale)
}

private fun LocalDate.format(skeleton: String, locale: Locale): String {
    return toJavaLocalDate().format(localizedFormatter(skeleton, locale)).titlecaseFirstChar(locale)
}
//endregion
