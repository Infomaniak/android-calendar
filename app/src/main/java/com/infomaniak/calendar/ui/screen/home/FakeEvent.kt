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
package com.infomaniak.calendar.ui.screen.home

import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Attendee
import com.infomaniak.multiplatform_calendar.core.domain.model.event.AttendeeRole
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventEnd
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import com.infomaniak.multiplatform_calendar.core.domain.model.event.ParticipationStatus
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal data class FakeEvent(
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

@OptIn(ExperimentalTime::class)
internal val fakeCalendarId = CalendarId("https://caldav.example.com/calendars/user/personal/")

@OptIn(ExperimentalTime::class)
internal val fakeEventList: List<Event> = listOf(
    FakeEvent(
        id = EventId("https://caldav.example.com/calendars/user/personal/weekly-sync.ics"),
        calendarId = fakeCalendarId,
        title = "Weekly Sync",
        description = "Team standup and weekly planning session.",
        location = "Meeting Room A",
        categories = "Work",
        timing = EventTiming.Timed(
            start = Instant.parse("2026-06-15T10:00:00Z"),
            end = EventEnd.At(Instant.parse("2026-06-15T11:00:00Z")),
        ),
        lastModified = Instant.parse("2026-06-10T08:30:00Z"),
        attendees = listOf(
            Attendee(
                email = "alice@example.com",
                displayName = "Alice Martin",
                status = ParticipationStatus.Accepted,
                role = AttendeeRole.Organizer,
                isOrganizer = true,
            ),
            Attendee(
                email = "bob@example.com",
                displayName = "Bob Dupont",
                status = ParticipationStatus.Accepted,
                role = AttendeeRole.Requested,
            ),
        ),
        organizer = Attendee(
            email = "alice@example.com",
            displayName = "Alice Martin",
            status = ParticipationStatus.Accepted,
            role = AttendeeRole.Organizer,
            isOrganizer = true,
        ),
        color = 0xFF2196F3.toInt(),
    ),
    FakeEvent(
        id = EventId("https://caldav.example.com/calendars/user/personal/team-lunch.ics"),
        calendarId = fakeCalendarId,
        title = "Team Lunch",
        description = "Monthly team lunch at the Italian place around the corner.",
        location = "Ristorante Bella Italia",
        categories = "Social",
        timing = EventTiming.Timed(
            start = Instant.parse("2026-06-17T12:30:00Z"),
            end = EventEnd.At(Instant.parse("2026-06-17T13:30:00Z")),
        ),
        lastModified = Instant.parse("2026-06-11T09:00:00Z"),
        color = 0xFFE91E63.toInt(),
        canEdit = false,
    ),
    FakeEvent(
        id = EventId("https://caldav.example.com/calendars/user/personal/design-review.ics"),
        calendarId = fakeCalendarId,
        title = "Design Review",
        description = "Review new UI mockups with the design team.",
        location = "Meeting Room B",
        categories = "Work",
        timing = EventTiming.Timed(
            start = Instant.parse("2026-06-16T15:00:00Z"),
            end = EventEnd.At(Instant.parse("2026-06-16T16:00:00Z")),
        ),
        lastModified = Instant.parse("2026-06-13T11:00:00Z"),
        color = 0xFF9C27B0.toInt(),
    ),
    FakeEvent(
        id = EventId("https://caldav.example.com/calendars/user/personal/hiking-day.ics"),
        calendarId = fakeCalendarId,
        title = "Hiking Day",
        description = "Annual company hiking trip.",
        location = "Salève, France",
        categories = "Sport",
        timing = EventTiming.Timed(
            start = Instant.parse("2026-06-20T00:00:00Z"),
            end = EventEnd.At(Instant.parse("2026-06-20T00:30:00Z")),
        ),
        lastModified = Instant.parse("2026-06-05T14:00:00Z"),
        color = 0xFF4CAF50.toInt(),
    ),
    FakeEvent(
        id = EventId("https://caldav.example.com/calendars/user/personal/product-review.ics"),
        calendarId = fakeCalendarId,
        title = "Product Review",
        description = "Quarterly product roadmap review with the PM and engineering leads.",
        location = "Conf Room B / Zoom",
        categories = "Work",
        timing = EventTiming.Timed(
            start = Instant.parse("2026-06-22T14:00:00Z"),
            end = EventEnd.At(Instant.parse("2026-06-22T15:30:00Z")),
        ),
        lastModified = Instant.parse("2026-06-12T10:00:00Z"),
        attendees = listOf(
            Attendee(
                email = "carol@example.com",
                displayName = "Carol Schmidt",
                status = ParticipationStatus.Accepted,
                role = AttendeeRole.Organizer,
                isOrganizer = true,
            ),
            Attendee(
                email = "dave@example.com",
                displayName = "Dave Leroy",
                status = ParticipationStatus.Tentative,
                role = AttendeeRole.Requested,
                responseNeeded = true,
            ),
        ),
        color = 0xFFFF9800.toInt(),
    ),
)

@OptIn(ExperimentalTime::class)
internal val fakeEvents: EventsByWeekAndDay = fakeEventList.groupByWeekAndDay()
