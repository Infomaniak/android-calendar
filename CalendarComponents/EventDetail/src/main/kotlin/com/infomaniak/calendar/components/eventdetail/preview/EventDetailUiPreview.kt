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
package com.infomaniak.calendar.components.eventdetail.preview

import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.infomaniak.calendar.components.eventdetail.models.EventColorChoice
import com.infomaniak.calendar.components.eventdetail.models.EventDetailRecurrence
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.NotificationTime
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.LocationUi
import com.infomaniak.calendar.components.foundation.preview.previewAttendees
import kotlinx.datetime.TimeZone
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

val previewEventDetailUi = EventDetailUi(
    eventColorChoice = EventColorChoice.Default,
    calendar = previewEventDetailCalendar,
    calendars = previewEventDetailCalendarGroups,
    title = "Event Title",
    recurrence = EventDetailRecurrence.Recurrence(getText = { "Every week on Monday" }),
    start = EventDetailTiming.Precise(Instant.parse("2026-05-20T08:00:00Z"), TimeZone.of("Europe/Paris")),
    end = EventDetailTiming.Precise(Instant.parse("2026-05-20T09:00:00Z"), TimeZone.of("Europe/Paris")),
    isAllDay = false,
    attendees = Attendees(
        all = previewAttendees,
        me = previewAttendees.first(),
    ),
    organizer = null,
    kMeetUrl = "test url",
    location = LocationUi.Room(id = "1", name = "Japan room", seats = 5, floor = 3, address = "Infomaniak\nRue Eugène-Marziano 25, 1227 Les Acacias", availability = null),
    urlLink = null,
    description = LoremIpsum(30).values.first(),
    files = listOf(
        EventDetailUi.File("1", "How to not get fired.pdf"),
        EventDetailUi.File("2", "Bob.txt"),
        EventDetailUi.File("3", "Next loto results.png"),
    ),
    notifications = listOf(
        EventDetailUi.Notification(
            EventDetailUi.Notification.Type.Email,
            NotificationTime.Absolute(Instant.parse("2026-05-20T07:00:00Z")),
        ),
        EventDetailUi.Notification(
            EventDetailUi.Notification.Type.Push,
            NotificationTime.Offset(90.days + 30.minutes),
        ),
    ),
    isOccupied = true,
    classification = EventDetailUi.Classification.Public,
    canEdit = true,
)
