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
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toKotlinTimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
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

/**
 * `Wednesday, May 20, 08:00 - 09:00 (GMT+2)`, dropping the date when the range is on a single day, and repeating the offset on
 * each bound when the two differ. Differs from [formatDateTimeRange] as it will drop the day of week and month/day if [start]
 * and [end] are on the same day.
 */
@Composable
fun formatDateTimeRangeWithZone(
    start: LocalDateTime,
    startUtcOffset: UtcOffset,
    end: LocalDateTime,
    endUtcOffset: UtcOffset,
    currentYear: Int = Clock.today(ZoneId.ofOffset("UTC", ZoneOffset.ofTotalSeconds(startUtcOffset.totalSeconds)).toKotlinTimeZone()).year,
): String = formatDateTimeRangeWithZone(
    start = start,
    startUtcOffset = startUtcOffset,
    end = end,
    endUtcOffset = endUtcOffset,
    locale = currentLocale(),
    use24HourFormat = isUsing24HourFormat(),
    currentYear = currentYear,
)

/** `Wednesday, May 20 - Friday, May 22`, or a single date when both bounds land on the same day. Both are inclusive. */
@Composable
fun formatDateRange(
    startDate: LocalDate,
    endDate: LocalDate,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    currentYear: Int = Clock.today(timeZone).year,
): String = formatDateRange(startDate, endDate, currentLocale(), currentYear)
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

    return joinRange(formattedStart, formattedEnd)
}

internal fun formatDateTimeRange(
    start: LocalDateTime,
    end: LocalDateTime,
    locale: Locale,
    use24HourFormat: Boolean,
    currentYear: Int,
): String = assembleDateTimeRange(
    start = start,
    end = end,
    formattedStartTime = start.time.formatTime(locale, use24HourFormat),
    formattedEndTime = end.time.formatTime(locale, use24HourFormat),
    locale = locale,
    currentYear = currentYear,
)

internal fun formatDateTimeRangeWithZone(
    start: LocalDateTime,
    startUtcOffset: UtcOffset,
    end: LocalDateTime,
    endUtcOffset: UtcOffset,
    locale: Locale,
    use24HourFormat: Boolean,
    currentYear: Int,
): String {
    val startTime = start.time.formatTime(locale, use24HourFormat)
    val endTime = end.time.formatTime(locale, use24HourFormat)
    val startOffset = start.formatZoneOffset(startUtcOffset, locale)
    val endOffset = end.formatZoneOffset(endUtcOffset, locale)

    val isSingleDay = start.date == end.date
    val hasSingleOffset = isSingleDay && startOffset == endOffset

    fun assembleDateTimeRangeWithZone(formattedStartTime: String, formattedEndTime: String): String = if (isSingleDay) {
        joinRange(formattedStartTime, formattedEndTime)
    } else {
        assembleDateTimeRange(start, end, formattedStartTime, formattedEndTime, locale, currentYear)
    }

    return if (hasSingleOffset) {
        assembleDateTimeRangeWithZone(formattedStartTime = startTime, formattedEndTime = endTime).withOffset(startOffset)
    } else {
        assembleDateTimeRangeWithZone(
            formattedStartTime = startTime.withOffset(startOffset),
            formattedEndTime = endTime.withOffset(endOffset),
        )
    }
}

/** Only add the whole "date" information to the end date if it's different from the start date */
private fun assembleDateTimeRange(
    start: LocalDateTime,
    end: LocalDateTime,
    formattedStartTime: String,
    formattedEndTime: String,
    locale: Locale,
    currentYear: Int,
): String {
    fun LocalDateTime.withFormattedTime(formattedTime: String): String {
        return joinDateAndTime(date.formatFullDate(locale, currentYear), formattedTime, locale)
    }

    val formattedStart = start.withFormattedTime(formattedStartTime)
    val formattedEnd = if (start.date == end.date) formattedEndTime else end.withFormattedTime(formattedEndTime)

    return joinRange(formattedStart, formattedEnd)
}

internal fun formatDateRange(startDate: LocalDate, endDate: LocalDate, locale: Locale, currentYear: Int): String {
    val formattedStart = startDate.formatFullDate(locale, currentYear)

    return if (startDate == endDate) {
        formattedStart
    } else {
        joinRange(formattedStart, endDate.formatFullDate(locale, currentYear))
    }
}
//endregion

private fun joinRange(start: String, end: String) = start + RANGE_SEPARATOR + end

private fun String.withOffset(offset: String) = "$this ($offset)"
