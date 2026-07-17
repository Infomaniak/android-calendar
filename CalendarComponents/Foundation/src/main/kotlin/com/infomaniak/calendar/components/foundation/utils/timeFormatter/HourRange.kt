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
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Formats a time range, showing only the hours when a bound happens today, otherwise prefixing that hour with its day
 * (and the year, when different from the current one), e.g. "10:00 - 28 August, 12:00" or
 * "27 August, 10:00 - 28 August, 12:00".
 */
@Composable
fun LocalDateTime.formatRangeTo(end: LocalDateTime): String {
    val today = LocalDate.now()
    val startDate = toLocalDate()
    val endDate = end.toLocalDate()

    val start = if (startDate == today) formatHours() else "${startDate.formatDayMonth()}, ${formatHours()}"
    val finish = if (endDate == today) end.formatHours() else "${endDate.formatDayMonth()}, ${end.formatHours()}"

    return "$start - $finish"
}
