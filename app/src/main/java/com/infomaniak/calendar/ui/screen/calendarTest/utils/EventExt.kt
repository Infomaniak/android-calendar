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
package com.infomaniak.calendar.ui.screen.calendarTest.utils

import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlin.time.ExperimentalTime


private val dateTimeFormatter = LocalDateTime.Format {
    date(LocalDate.Formats.ISO)
    char(' ')
    hour(); char(':'); minute()
}

@OptIn(ExperimentalTime::class)
internal fun Event.toUi(): com.infomaniak.calendar.ui.screen.calendarTest.model.EventUi =
    _root_ide_package_.com.infomaniak.calendar.ui.screen.calendarTest.model.EventUi(
        id = id.url,
        title = title.ifBlank { "(no title)" },
        timeRange = timeRange(isAllDay, start, end),
        location = location?.takeIf { it.isNotBlank() },
        status = status?.takeIf { it.isNotBlank() },
        categories = categories?.takeIf { it.isNotBlank() },
        description = description?.takeIf { it.isNotBlank() },
        lastModified = lastModified?.format(dateTimeFormatter),
    )

private fun timeRange(
    isAllDay: Boolean,
    start: LocalDateTime?,
    end: LocalDateTime?,
): String? = when {
    isAllDay -> "All day"
    start != null && end != null -> "${start.format(dateTimeFormatter)} → ${end.format(dateTimeFormatter)}"
    else -> null
}
