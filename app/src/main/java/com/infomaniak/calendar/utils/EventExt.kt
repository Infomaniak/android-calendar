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
import com.infomaniak.calendar.components.eventdetail.models.NotificationTime
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.Calendar
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Classification
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventBounds
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import com.infomaniak.multiplatform_calendar.core.domain.model.event.TimeBlocking
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.AlarmAction
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.AlarmTrigger
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.EventAlarm
import com.infomaniak.multiplatform_calendar.core.domain.model.event.toLocalizedRecurrenceString
import com.infomaniak.multiplatform_core.account.domain.model.AccountId
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

/**
 * Translation of the KMP event model into the UI model consumed by the EventDetail component,
 * shared by every view that opens an event so they all display it identically.
 */
fun Event.toEventDetailUi(calendar: Calendar, emailsByUserId: Map<AccountId, String>): EventDetailUi = EventDetailUi(
    eventColor = Color(colors.sourceColor),
    calendarColor = Color(colors.calendarSourceColor.argb),
    calendarName = calendar.displayName,
    title = title,
    start = timing.toDetailTiming(isStart = true),
    end = timing.toDetailTiming(isStart = false),
    isAllDay = timing.isAllDay,
    getRecurrenceText = if (isOccurrence) ::toLocalizedRecurrenceString else null,
    attendees = attendees.toAttendees(accountId, emailsByUserId),
    kMeetUrl = null, // TODO[eventDetail]: Not carried by the KMP model yet
    location = location,
    room = null, // TODO[eventDetail]: Not carried by the KMP model yet
    urlLink = null, // TODO[eventDetail]: Not carried by the KMP model yet
    description = description,
    files = emptyList(), // TODO[eventDetail]: Not carried by the KMP model yet
    notifications = alarms.mapNotNull { it.toNotification() },
    isOccupied = timeBlocking != TimeBlocking.DoesNotBlock, // null is considered as occupied
    classification = classification.toClassification(),
    canEdit = canEdit,
)

private fun EventTiming.toDetailTiming(isStart: Boolean): EventDetailTiming = when (val bounds = bounds) {
    is EventBounds.AllDay -> EventDetailTiming.Floating(
        LocalDateTime(date = if (isStart) bounds.start else bounds.end, time = LocalTime(0, 0)),
    )
    is EventBounds.Floating -> EventDetailTiming.Floating(if (isStart) bounds.start else bounds.end)
    is EventBounds.Zoned -> (if (isStart) bounds.start else bounds.end).let {
        EventDetailTiming.Precise(it.instant, it.timeZone)
    }
}

private fun EventAlarm.toNotification(): EventDetailUi.Notification? {

    return EventDetailUi.Notification(
        type = when (action) {
            AlarmAction.Display -> EventDetailUi.Notification.Type.Push
            AlarmAction.Email -> EventDetailUi.Notification.Type.Email
            else -> return null // Other types of alarms are not supported in the UI yet
        },
        time = when (val trigger = trigger) {
            is AlarmTrigger.Relative -> NotificationTime.Offset(trigger.offset)
            is AlarmTrigger.Absolute -> NotificationTime.Absolute(trigger.instant)
        },
    )
}

private fun Classification?.toClassification(): EventDetailUi.Classification? = when (this) {
    null,
    Classification.Public -> EventDetailUi.Classification.Public
    Classification.Private -> EventDetailUi.Classification.Private
    Classification.Confidential -> EventDetailUi.Classification.Confidential
    is Classification.Custom -> null
}
