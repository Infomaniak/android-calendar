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
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalTime
import java.util.Locale

private const val TIME_24_HOURS_PATTERN = "HH:mm"
private const val TIME_12_HOURS_PATTERN = "hh:mm a"
private const val SHORT_TIME_12_HOURS_PATTERN = "h a"

//region Exposed formatting methods
/** `06:00` or `6 AM`, narrow enough for the timeline's hour gutter. */
@Composable
fun LocalTime.formatShortTimeLabel(): String {
    val pattern = if (isUsing24HourFormat()) TIME_24_HOURS_PATTERN else SHORT_TIME_12_HOURS_PATTERN

    return toJavaLocalTime().format(fixedFormatter(pattern, currentLocale()))
}
//endregion

//region Underlying testable logic
/** `08:00`, or `08:00 AM` when the user is not on 24 hours format. */
internal fun LocalTime.formatTime(locale: Locale, use24HourFormat: Boolean): String {
    val pattern = if (use24HourFormat) TIME_24_HOURS_PATTERN else TIME_12_HOURS_PATTERN

    return toJavaLocalTime().format(fixedFormatter(pattern, locale))
}
//endregion
