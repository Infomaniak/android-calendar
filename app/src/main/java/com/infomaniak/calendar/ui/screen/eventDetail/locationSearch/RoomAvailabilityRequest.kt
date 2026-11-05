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
package com.infomaniak.calendar.ui.screen.eventDetail.locationSearch

import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.utils.eventBounds
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventBounds
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import kotlinx.datetime.atTime

/**
 * The rooms of [calendarId] must be free over [bounds], and on every occurrence when [editedOccurrenceId] belongs to a recurring
 * event.
 */
data class RoomAvailabilityRequest(val calendarId: CalendarId, val bounds: EventBounds, val editedOccurrenceId: OccurrenceId?)

fun EventFormState.toRoomAvailabilityRequest(editedOccurrenceId: OccurrenceId?): RoomAvailabilityRequest {
    val (start, end) = dateAndTimeState.start to dateAndTimeState.end
    return RoomAvailabilityRequest(
        calendarId = CalendarId(calendar.id),
        bounds = eventBounds(
            isAllDay = isAllDay.value,
            start = start.date.atTime(start.time),
            startTimeZone = start.timeZone,
            end = end.date.atTime(end.time),
            endTimeZone = end.timeZone,
        ),
        editedOccurrenceId = editedOccurrenceId,
    )
}
