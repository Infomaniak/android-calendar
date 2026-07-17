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

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLocale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.time.LocalDate as JavaLocalDate

//region Kotlin
fun LocalDate.monthYearLabel(locale: Locale, currentYear: Int): String {
    val month = java.time.Month.of(month.number)
        .getDisplayName(TextStyle.FULL_STANDALONE, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

    return if (year == currentYear) month else "$month $year"
}
//endregion

//region Java
/**
 * Format a date either as day and month if it is this year or day, month and year if it's another year.
 */
@Composable
internal fun JavaLocalDate.formatDayMonth(): String {
    val locale = LocalLocale.current.platformLocale
    val skeleton = if (year == now().year) "dMMMM" else "dMMMMy"
    val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)

    return format(DateTimeFormatter.ofPattern(pattern, locale))
}
//endregion
