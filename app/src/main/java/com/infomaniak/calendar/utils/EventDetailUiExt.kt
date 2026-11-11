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

import androidx.compose.foundation.text.input.TextFieldState
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.foundation.models.OrganizerSource
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus

fun EventDetailUi.toEventFormState(): EventFormState {
    val start = start.atTimeZone
    val end = end.atTimeZone
    // KMP ends an all-day event on the day after it, the form shows its last day
    val endDate = if (isAllDay) end.date.minus(1, DateTimeUnit.DAY) else end.date

    return EventFormState(
        titleTextState = TextFieldState(title),
        calendarId = calendar.id,
        eventColor = eventColorChoice,
        isAllDay = isAllDay,
        start = start,
        startTimeZone = this.start.timeZone,
        end = endDate.atTime(end.time),
        endTimeZone = this.end.timeZone,
        hasEndEverBeenTouched = false,
        participants = attendees.all.toSet(),
        organizerSource = OrganizerSource.Static(organizer),
        isKMeetUrlEnabled = kMeetUrl != null,
        notifications = notifications,
        isOccupied = isOccupied,
        isPublic = classification == EventDetailUi.Classification.Public,
        calendars = calendars,
    )
}

private val EventDetailTiming.timeZone: TimeZone
    get() = (this as? EventDetailTiming.Precise)?.timeZone ?: TimeZone.currentSystemDefault()
