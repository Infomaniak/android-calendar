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
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.HourFormatter.formatHours
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Formats a time range, showing only the hours when a bound happens today, otherwise prefixing that hour with its day
 * (and the year, when different from the current one), e.g. "10:00 - 28 August, 12:00" or
 * "27 August, 10:00 - 28 August, 12:00".
 */
@Composable
fun Instant.formatRangeTo(end: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val today = Clock.today(timeZone)
    val isStartToday = toLocalDateTime(timeZone).date == today
    val isEndToday = end.toLocalDateTime(timeZone).date == today

    val zoneId = timeZone.toJavaZoneId()
    val start = if (isStartToday) formatHours(zoneId) else "${formatDayMonth(timeZone)}, ${formatHours(zoneId)}"
    val finish = if (isEndToday) end.formatHours(zoneId) else "${end.formatDayMonth(timeZone)}, ${end.formatHours(zoneId)}"

    return "$start - $finish"
}
