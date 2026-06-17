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
package com.infomaniak.calendar.ui.screen.eventFormTest.utils

import com.infomaniak.calendar.ui.screen.eventFormTest.model.CalendarChoice
import com.infomaniak.calendar.ui.screen.eventFormTest.model.EventFormData
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.Calendar
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventEditData
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventEnd
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

internal fun Calendar.toChoice() = CalendarChoice(id = id, name = displayName, color = color)
@OptIn(ExperimentalTime::class)
internal fun Event.toFormData(): EventFormData {
    val isAllDay = timing is EventTiming.AllDay
    val start = timing.formStart()
    val end = timing.formEnd()
    return EventFormData(
        title = title,
        isAllDay = isAllDay,
        start = start,
        end = end,
        location = location.orEmpty(),
        description = description.orEmpty(),
        calendarId = calendarId,
    )
}
@OptIn(ExperimentalTime::class)
internal fun EventFormData.toEditData(): EventEditData = EventEditData(
    title = title,
    timing = if (isAllDay) {
        EventTiming.AllDay(startDate = start.date, endDate = end.date)
    } else {
        EventTiming.Timed(
            start = start.toInstant(TimeZone.UTC),
            end = EventEnd.At(end.toInstant(TimeZone.UTC)),
        )
    },
    location = location.ifBlank { null },
    description = description.ifBlank { null },
    calendarId = calendarId,
)
@OptIn(ExperimentalTime::class)
private fun EventTiming.formStart(): LocalDateTime = when (this) {
    is EventTiming.AllDay -> LocalDateTime(startDate, LocalTime(0, 0))
    is EventTiming.Timed -> start.toLocalDateTime(TimeZone.UTC)
}
@OptIn(ExperimentalTime::class)
private fun EventTiming.formEnd(): LocalDateTime = when (this) {
    is EventTiming.AllDay -> LocalDateTime(endDate, LocalTime(0, 0))
    is EventTiming.Timed -> resolvedEnd().toLocalDateTime(TimeZone.UTC)
}

