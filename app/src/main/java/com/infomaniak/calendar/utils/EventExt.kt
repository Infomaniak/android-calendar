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
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.Calendar
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Classification
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventDateTime
import com.infomaniak.multiplatform_calendar.core.domain.model.event.TimeBlocking
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.EventAlarm

/**
 * Translation of the KMP event model into the UI model consumed by the EventDetail component,
 * shared by every view that opens an event so they all display it identically.
 */
fun Event.toEventDetailUi(calendar: Calendar, emailsByUserId: Map<AccountId, String>): EventDetailUi = EventDetailUi(
    eventColor = Color(colors.sourceColor),
    calendarColor = Color(colors.calendarSourceColor.argb),
    calendarName = calendar.displayName,
    title = title,
    start = timing.start.toDetailTiming(),
    end = timing.end.toDetailTiming(),
    isAllDay = timing.isAllDay,
    attendees = attendees.toAttendees(accountId, emailsByUserId),
    kMeetUrl = null, // TODO[eventDetail]: Not carried by the KMP model yet
    location = location,
    room = null, // TODO[eventDetail]: Not carried by the KMP model yet
    urlLink = null, // TODO[eventDetail]: Not carried by the KMP model yet
    description = description,
    files = emptyList(), // TODO[eventDetail]: Not carried by the KMP model yet
    notifications = alarms.mapNotNull { it.toNotification() },
    isOccupied = timeBlocking != TimeBlocking.DoesNotBlock, // null is considered as occupied
    classification = classification?.toClassification(),
    canEdit = canEdit,
)

private fun EventDateTime.toDetailTiming(): EventDetailTiming = when (this) {
    is EventDateTime.Floating -> EventDetailTiming.Floating(wallClock)
    is EventDateTime.Precise -> EventDetailTiming.Precise(instant, timeZone)
}

// TODO[eventDetail]: Handle notifications
private fun EventAlarm.toNotification(): EventDetailUi.Notification? {
    return null
}

private fun Classification.toClassification(): EventDetailUi.Classification? = when (this) {
    Classification.Public -> EventDetailUi.Classification.Public
    Classification.Private -> EventDetailUi.Classification.Private
    Classification.Confidential -> EventDetailUi.Classification.Confidential
    is Classification.Custom -> null
}
