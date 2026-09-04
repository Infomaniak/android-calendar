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

import androidx.compose.ui.graphics.Color
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.EventAlarm
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * Translation of the KMP event model into the UI model consumed by the EventDetail component,
 * shared by every view that opens an event so they all display it identically.
 *
 * [timeZone] anchors the wall-clocks the event leaves unanchored (floating and all-day ones).
 */
fun Event.toEventDetailUi(
    emailsByUserId: Map<AccountId, String>,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): EventDetailUi = EventDetailUi(
    eventColor = Color(colors.sourceColor),
    calendarColor = Color(colors.calendarSourceColor.argb),
    title = title,
    start = getDetailTiming(timing.start, timing.startTimeZone),
    end = getDetailTiming(timing.end, timing.endTimeZone),
    isAllDay = timing.isAllDay,
    attendees = attendees.toAttendees(accountId, emailsByUserId),
    kMeetUrl = null, // TODO[eventDetail]: Not carried by the KMP model yet
    location = location,
    room = null, // TODO[eventDetail]: Not carried by the KMP model yet
    urlLink = null, // TODO[eventDetail]: Not carried by the KMP model yet
    description = description,
    files = emptyList(), // TODO[eventDetail]: Not carried by the KMP model yet
    notifications = alarms.mapNotNull { it.toNotification(timing, timeZone) },
)

/**
 *  A wall-clock without a time zone is floating: it is read in whatever zone the reader is in.
 *
 *  A wall-clock + a time zone is ambiguous during DST repeated hours. During such a day, like the 25th of oct. 2026,
 *  2:30 PM occurs twice at the Europe/Paris time zone. This conversion method handles the collapsing of the both possibile
 *  instants into a single one.
 **/
private fun getDetailTiming(wallClock: LocalDateTime, timeZone: TimeZone?): EventDetailTiming = when (timeZone) {
    null -> EventDetailTiming.Floating(wallClock)
    else -> EventDetailTiming.Precised(wallClock.toInstant(timeZone), timeZone)
}

private fun EventAlarm.toNotification(timing: EventTiming, timeZone: TimeZone): EventDetailUi.Notification? {
    return null
}
