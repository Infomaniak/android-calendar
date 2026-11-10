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

import com.infomaniak.calendar.components.eventdetail.form.EventFormValues
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

fun EventDetailUi.toEventFormValues(): EventFormValues {
    val end = end.atTimeZone

    return EventFormValues(
        calendars = calendars,
        calendar = calendar,
        eventColor = eventColorChoice,
        title = title,
        isAllDay = isAllDay,
        start = start.atTimeZone,
        startTimeZone = start.timeZone,
        // KMP ends an all-day event on the day after it, the form shows its last day
        end = if (isAllDay) LocalDateTime(end.date.minus(1, DateTimeUnit.DAY), end.time) else end,
        endTimeZone = this.end.timeZone,
        isKMeetUrlEnabled = kMeetUrl != null,
        notifications = notifications,
        isOccupied = isOccupied,
        isPublic = classification == EventDetailUi.Classification.Public,
    )
}

private val EventDetailTiming.timeZone: TimeZone
    get() = (this as? EventDetailTiming.Precise)?.timeZone ?: TimeZone.currentSystemDefault()
