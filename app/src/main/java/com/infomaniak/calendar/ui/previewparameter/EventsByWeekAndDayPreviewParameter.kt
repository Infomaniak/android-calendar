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
package com.infomaniak.calendar.ui.previewparameter

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.infomaniak.calendar.ui.screen.planning.EventsByWeekAndDay
import com.infomaniak.calendar.ui.screen.planning.groupByWeekAndDay
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Attendee
import com.infomaniak.multiplatform_calendar.core.domain.model.event.AttendeeRole
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import com.infomaniak.multiplatform_calendar.core.domain.model.event.ParticipationStatus
import kotlin.time.Instant

class EventsByWeekAndDayPreviewParameter : PreviewParameterProvider<EventsByWeekAndDay> {
    override val values: Sequence<EventsByWeekAndDay> = sequenceOf(fakeEventList.groupByWeekAndDay())
}

private val fakeCalendarId = CalendarId("https://caldav.example.com/calendars/user/personal/")

private fun eventId(slug: String) = EventId("https://caldav.example.com/calendars/user/personal/$slug.ics")

private fun timed(start: String, end: String) = EventTiming.Timed(
    start = Instant.parse(start),
    end = Instant.parse(end),
)

private val alice = Attendee(
    email = "alice@example.com",
    displayName = "Alice Martin",
    status = ParticipationStatus.Accepted,
    role = AttendeeRole.Organizer,
    isOrganizer = true,
)

private val bob = Attendee(
    email = "bob@example.com",
    displayName = "Bob Dupont",
    status = ParticipationStatus.Accepted,
    role = AttendeeRole.Requested,
)

private val fakeEventList: List<Event> = listOf(
    FakeEvent(
        id = eventId("weekly-sync"),
        calendarId = fakeCalendarId,
        title = "Weekly Sync",
        description = "Team standup and weekly planning session.",
        location = "Meeting Room A",
        timing = timed("2026-06-15T10:00:00Z", "2026-06-15T11:00:00Z"),
        attendees = listOf(alice, bob),
        organizer = alice,
    ),
    FakeEvent(
        id = eventId("team-lunch"),
        calendarId = fakeCalendarId,
        title = "Team Lunch",
        description = "Monthly team lunch at the Italian place around the corner.",
        location = "Ristorante Bella Italia",
        timing = timed("2026-06-17T12:30:00Z", "2026-06-17T13:30:00Z"),
        color = 0xFFE91E63.toInt(),
        canEdit = false,
    ),
    FakeEvent(
        id = eventId("design-review"),
        calendarId = fakeCalendarId,
        title = "Design Review",
        description = "Review new UI mockups with the design team.",
        location = "Meeting Room B",
        timing = timed("2026-06-16T15:00:00Z", "2026-06-16T16:00:00Z"),
        color = 0xFF9C27B0.toInt(),
    ),
    FakeEvent(
        id = eventId("hiking-day"),
        calendarId = fakeCalendarId,
        title = "Hiking Day",
        description = "Annual company hiking trip.",
        location = "Salève, France",
        timing = timed("2026-06-20T00:00:00Z", "2026-06-20T00:30:00Z"),
        color = 0xFF4CAF50.toInt(),
    ),
    FakeEvent(
        id = eventId("product-review"),
        calendarId = fakeCalendarId,
        title = "Product Review",
        description = "Quarterly product roadmap review with the PM and engineering leads.",
        location = "Conf Room B / kMeet",
        timing = timed("2026-06-22T14:00:00Z", "2026-06-22T15:30:00Z"),
        attendees = listOf(alice, bob),
        color = 0xFFFF9800.toInt(),
    ),
)

private data class FakeEvent(
    override val id: EventId,
    override val calendarId: CalendarId,
    override val title: String,
    override val description: String? = null,
    override val location: String? = null,
    override val status: String? = "CONFIRMED",
    override val categories: String? = null,
    override val timing: EventTiming,
    override val lastModified: Instant? = null,
    override val attendees: List<Attendee> = emptyList(),
    override val organizer: Attendee? = null,
    override val color: Int = 0xFF2196F3.toInt(),
    override val canEdit: Boolean = true,
) : Event
