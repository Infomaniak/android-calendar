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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Instant

private const val RANGE_SEPARATOR = " - "

//region Exposed formatting methods
/** `08:00 - 09:00`. Only for single day time ranges */
@Composable
fun formatTimeRange(start: Instant, end: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    return formatTimeRange(start, end, currentLocale(), timeZone, isUsing24HourFormat())
}

/** `Wednesday, May 20, 08:00 - 09:00`, repeating the date on the end when the range spans several days. */
@Composable
fun formatDateTimeRange(
    start: LocalDateTime,
    end: LocalDateTime,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    currentYear: Int = Clock.today(timeZone).year,
): String = formatDateTimeRange(start, end, currentLocale(), isUsing24HourFormat(), currentYear)

/** `Wednesday, May 20 - Friday, May 22`, or a single date when both bounds land on the same day. Both are inclusive. */
@Composable
fun formatDateRange(
    start: LocalDateTime,
    end: LocalDateTime,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    currentYear: Int = Clock.today(timeZone).year,
): String = formatDateRange(start.date, end.date, currentLocale(), currentYear)
//endregion

//region Underlying testable logic
internal fun formatTimeRange(
    start: Instant,
    end: Instant,
    locale: Locale,
    timeZone: TimeZone,
    use24HourFormat: Boolean,
): String {
    val formattedStart = start.toLocalDateTime(timeZone).time.formatTime(locale, use24HourFormat)
    val formattedEnd = end.toLocalDateTime(timeZone).time.formatTime(locale, use24HourFormat)

    return formattedStart + RANGE_SEPARATOR + formattedEnd
}

internal fun formatDateTimeRange(
    start: LocalDateTime,
    end: LocalDateTime,
    locale: Locale,
    use24HourFormat: Boolean,
    currentYear: Int,
): String {
    val formattedEnd = if (start.date == end.date) {
        end.time.formatTime(locale, use24HourFormat)
    } else {
        end.formatDateAndTime(locale, use24HourFormat, currentYear)
    }

    return start.formatDateAndTime(locale, use24HourFormat, currentYear) + RANGE_SEPARATOR + formattedEnd
}

private fun LocalDateTime.formatDateAndTime(locale: Locale, use24HourFormat: Boolean, currentYear: Int): String {
    return joinDateAndTime(date.formatFullDate(locale, currentYear), time.formatTime(locale, use24HourFormat), locale)
}

internal fun formatDateRange(startDate: LocalDate, endDate: LocalDate, locale: Locale, currentYear: Int): String {
    val formattedStart = startDate.formatFullDate(locale, currentYear)

    return if (startDate == endDate) {
        formattedStart
    } else {
        formattedStart + RANGE_SEPARATOR + endDate.formatFullDate(locale, currentYear)
    }
}
//endregion
