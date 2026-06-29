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
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

internal fun Calendar.toChoice() = CalendarChoice(id = id, name = displayName, color = color.argb)

internal fun Event.toFormData(): EventFormData {
    val isAllDay = timing.isAllDay
    val start = timing.start.toLocalDateTime(TimeZone.UTC)
    val end = timing.end.toLocalDateTime(TimeZone.UTC)
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

internal fun EventFormData.toEditData(): EventEditData = EventEditData(
    title = title,
    timing = EventTiming(
        start = start.toInstant(TimeZone.UTC),
        end = end.toInstant(TimeZone.UTC),
        isAllDay = isAllDay,
    ),
    location = location.ifBlank { null },
    description = description.ifBlank { null },
    calendarId = calendarId,
)
