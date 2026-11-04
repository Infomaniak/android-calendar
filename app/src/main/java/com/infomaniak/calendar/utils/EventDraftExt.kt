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

import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.components.eventdetail.models.NotificationTime
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.OrganizerUi
import com.infomaniak.calendar.ui.screen.eventDetail.AttendeesUpdatePayload
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.AlarmListEdit
import com.infomaniak.multiplatform_calendar.core.domain.model.event.AttendeeEdit
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventBounds
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventEditData
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Organizer
import com.infomaniak.multiplatform_calendar.core.domain.model.event.TimeBlocking
import com.infomaniak.multiplatform_calendar.core.domain.model.event.ZonedWallClock
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.AlarmAction
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.AlarmTrigger
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.EventAlarm
import com.infomaniak.multiplatform_calendar.core.domain.model.event.alarm.TriggerRelation
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

val EventDraft.typedCalendarId: CalendarId get() = CalendarId(calendarId)

/**
 * Writes the form's fields for both creation and edition: a field added to the form only needs its line here.
 * [defaultOrganizer] organizes the event when it gets attendees without having an organizer yet.
 */
fun EventDraft.applyTo(
    editData: EventEditData,
    storedAlarms: List<EventAlarm>,
    defaultOrganizer: Organizer?,
): EventEditData = editData.copy(
    title = title,
    timing = editData.timing.copy(bounds = bounds()),
    timeBlocking = if (isOccupied) TimeBlocking.Blocks else TimeBlocking.DoesNotBlock,
    calendarId = typedCalendarId,
    attendees = attendees.toAttendeeEdit(),
    organizer = (initialOrganizer?.toOrganizer() ?: defaultOrganizer).takeIf { attendees.isNotEmpty() },
    // Stored alarms KMP can't read are missing from storedAlarms, replacing the list would delete them
    alarms = if (notifications == storedAlarms.mapNotNull { it.toNotification() }) {
        editData.alarms
    } else {
        AlarmListEdit.Replace(notifications.toAlarms(storedAlarms))
    },
)

fun EventEditData.updateWithAttendeesAndOrganizer(
    attendeesUpdatePayload: AttendeesUpdatePayload,
    defaultOrganizer: Organizer?,
): EventEditData {
    return copy(
        attendees = attendeesUpdatePayload.attendees.toAttendeeEdit(),
        organizer = (attendeesUpdatePayload.organizer?.toOrganizer() ?: defaultOrganizer)
            .takeIf { attendeesUpdatePayload.attendees.isNotEmpty() },
    )
}

fun EventDraft.toNewEventEditData(defaultOrganizer: Organizer?): EventEditData = applyTo(
    storedAlarms = emptyList(),
    defaultOrganizer = defaultOrganizer,
    editData = EventEditData(
        title = "",
        timing = EventTiming(bounds = bounds()),
        location = null,
        description = null,
        timeBlocking = null,
        calendarId = typedCalendarId,
        eventColor = null,
        alarms = AlarmListEdit.Replace(emptyList()),
        attendees = emptyList(),
        organizer = null,
        meetRoom = null,
    ),
)

fun List<AttendeeUi>.toAttendeeEdit(): List<AttendeeEdit> = map { AttendeeEdit(email = it.email, displayName = it.displayName) }

/** The bounds the form describes, knowing it shows the last day of an all-day event where KMP expects the day after it. */
private fun EventDraft.bounds(): EventBounds = if (isAllDay) {
    EventBounds.AllDay(start = start.date, end = end.date.plus(1, DateTimeUnit.DAY))
} else {
    EventBounds.Zoned(start = ZonedWallClock(start, startTimeZone), end = ZonedWallClock(end, endTimeZone))
}

private fun OrganizerUi.toOrganizer(): Organizer = Organizer(email, displayName)

/**
 * A notification read from a stored alarm gives that alarm back, found by its id, so it keeps the properties the form doesn't show.
 * Stored alarms the form can't show are kept as well.
 */
private fun List<EventDetailUi.Notification>.toAlarms(storedAlarms: List<EventAlarm>): List<EventAlarm> {
    val alarms = map { notification ->
        notification.sourceId?.let { sourceId -> storedAlarms.find { it.id?.value == sourceId } } ?: notification.toAlarm()
    }

    return alarms + storedAlarms.filter { it.toNotification() == null }
}

private fun EventDetailUi.Notification.toAlarm(): EventAlarm = EventAlarm(
    action = when (type) {
        EventDetailUi.Notification.Type.Push -> AlarmAction.Display
        EventDetailUi.Notification.Type.Email -> AlarmAction.Email
    },
    trigger = when (val time = time) {
        is NotificationTime.Offset -> AlarmTrigger.Relative(time.duration, TriggerRelation.Start)
        is NotificationTime.Absolute -> AlarmTrigger.Absolute(time.instant)
    },
)
