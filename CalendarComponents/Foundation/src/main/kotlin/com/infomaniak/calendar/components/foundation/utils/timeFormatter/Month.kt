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
import com.infomaniak.calendar.components.foundation.utils.CapitalizationUtils.capitalizeFirstLetter
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Instant

fun LocalDate.monthYearLabel(locale: Locale, currentYear: Int): String {
    val month = java.time.Month.of(month.number)
        .getDisplayName(TextStyle.FULL_STANDALONE, locale)
        .capitalizeFirstLetter(locale)

    return if (year == currentYear) month else "$month $year"
}

/**
 * Format a date either as day and month if it is this year or day, month and year if it's another year.
 */
@Composable
internal fun Instant.formatDayMonth(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val locale = LocalLocale.current.platformLocale

    val date = toLocalDateTime(timeZone).date
    val currentYear = Clock.today(timeZone).year

    val skeleton = if (date.year == currentYear) "dMMMM" else "dMMMMy"
    val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)

    return date.toJavaLocalDate().format(DateTimeFormatter.ofPattern(pattern, locale))
}
