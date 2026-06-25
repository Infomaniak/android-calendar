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

import com.infomaniak.calendar.ui.screen.calendarTest.model.EventUi
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


private val dateTimeFormatter = LocalDateTime.Format {
    date(LocalDate.Formats.ISO)
    char(' ')
    hour(); char(':'); minute()
}

// TODO: Timezones are not handled yet — we render Instants in UTC.
@OptIn(ExperimentalTime::class)
private fun Instant.formatUtc(): String =
    toLocalDateTime(TimeZone.UTC).format(dateTimeFormatter)

@OptIn(ExperimentalTime::class)
fun Event.toUi(): EventUi = EventUi(
    id = id.url,
    title = title.ifBlank { "(no title)" },
    timeRange = timing.toTimeRange(),
    location = location?.takeIf { it.isNotBlank() },
    status = status?.takeIf { it.isNotBlank() },
    categories = categories?.takeIf { it.isNotBlank() },
    description = description?.takeIf { it.isNotBlank() },
    lastModified = lastModified?.formatUtc(),
    calendarColor = calendarColor,
    canEdit = canEdit,
)

@OptIn(ExperimentalTime::class)
private fun EventTiming.toTimeRange(): String = when (this) {
    is EventTiming.AllDay -> "All day"
    is EventTiming.Timed -> "${start.formatUtc()} → ${end.formatUtc()}"
}
