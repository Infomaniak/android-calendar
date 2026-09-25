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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.infomaniak.calendar.components.resources.R
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Instant

private const val SHORT_NUMERIC_DATE_SKELETON = "dM"
private const val SHORT_NUMERIC_DATE_WITH_YEAR_SKELETON = "yMd"

/** `9/17, 05:30PM`, showing the year as well if it's not the current year. */
@Composable
fun Instant.formatDateTime(
    timeZone: TimeZone,
    currentYear: Int,
): String {
    val locale = currentLocale()
    val use24HourFormat = isUsing24HourFormat()
    val dateTime = toLocalDateTime(timeZone)
    val formattedDate = dateTime.date.formatShortNumericDate(locale, currentYear)
    val formattedTime = dateTime.time.formatTime(locale, use24HourFormat)

    return joinDateAndTime(formattedDate, formattedTime, locale)
}

internal fun LocalDate.formatShortNumericDate(locale: Locale, currentYear: Int): String {
    val skeleton = if (year == currentYear) {
        SHORT_NUMERIC_DATE_SKELETON
    } else {
        SHORT_NUMERIC_DATE_WITH_YEAR_SKELETON
    }

    return format(skeleton, locale)
}

/** `1 hour, 30 minutes before`, `At the event time`, `2 days after`, weeks being the largest unit of duration we display. */

@Composable
fun Duration.formatDurationOffset(): String {
    if (absoluteValue.inWholeSeconds == 0L) {
        return stringResource(R.string.notificationTimeAtStart)
    }
    absoluteValue.toComponents { totalDays, hours, minutes, _, _ ->
        val weeks = (totalDays / DateTimeUnit.WEEK.days).toInt()
        val days = (totalDays % DateTimeUnit.WEEK.days).toInt()
        val parts = buildList {
            if (weeks > 0) add(pluralStringResource(R.plurals.weekAmount, weeks, weeks))
            if (days > 0) add(pluralStringResource(R.plurals.dayAmount, days, days))
            if (hours > 0) add(pluralStringResource(R.plurals.hourAmount, hours, hours))
            if (minutes > 0) add(pluralStringResource(R.plurals.minuteAmount, minutes, minutes))
        }
        val isBefore = isNegative()
        val durationText = parts.joinToString(", ")
        return if (isBefore) {
            stringResource(R.string.notificationTimeBefore, durationText)
        } else {
            stringResource(R.string.notificationTimeAfter, durationText)
        }
    }
}
