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
package com.infomaniak.calendar.utils

import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.AlarmListEdit
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventBounds
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventEditData
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import com.infomaniak.multiplatform_calendar.core.domain.model.event.TimeBlocking
import com.infomaniak.multiplatform_calendar.core.domain.model.event.ZonedWallClock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

/** Writes the form's fields for both creation and edition: a field added to the form only needs its line here. */
fun EventDraft.applyTo(editData: EventEditData): EventEditData = editData.copy(
    title = title,
    timing = editData.timing.copy(bounds = bounds()),
    timeBlocking = editData.timeBlocking.withOccupied(isOccupied),
)

fun EventDraft.toNewEventEditData(calendarId: CalendarId): EventEditData = applyTo(
    EventEditData(
        title = "",
        timing = EventTiming(bounds = bounds()),
        location = null,
        description = null,
        timeBlocking = null,
        calendarId = calendarId,
        eventColor = null,
        alarms = AlarmListEdit.Replace(emptyList()),
        attendees = emptyList(),
        organizer = null,
        meetRoom = null,
    ),
)

/** The bounds the form describes, knowing it shows the last day of an all-day event where KMP expects the day after it. */
private fun EventDraft.bounds(): EventBounds = if (isAllDay) {
    EventBounds.AllDay(start = start.date, end = end.date.plus(1, DateTimeUnit.DAY))
} else {
    EventBounds.Zoned(start = ZonedWallClock(start, startTimeZone), end = ZonedWallClock(end, endTimeZone))
}

/** The time blocking matching [isOccupied], keeping the current value when it already matches, `null` counting as occupied. */
private fun TimeBlocking?.withOccupied(isOccupied: Boolean): TimeBlocking? = when {
    isOccupied == (this != TimeBlocking.DoesNotBlock) -> this
    isOccupied -> TimeBlocking.Blocks
    else -> TimeBlocking.DoesNotBlock
}
