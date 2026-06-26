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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.ui.screen.planning.EventsByWeekAndDay
import com.infomaniak.calendar.ui.screen.planning.toEventColorsUi
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventColors
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

class EventsByWeekAndDayPreviewParameter : PreviewParameterProvider<EventsByWeekAndDay> {
    override val values: Sequence<EventsByWeekAndDay> = sequenceOf(fakeEventsByWeekAndDay)
}

private fun eventUi(
    id: String,
    title: String,
    start: String,
    end: String,
    location: String? = null,
    color: Color = Color(0xFF2196F3),
): EventUi {
    val attendees = listOf(
        AttendeeUi("alice@example.com", "Alice", ParticipationStatus.Accepted),
        AttendeeUi("bob@example.com", "Bob", ParticipationStatus.Tentative),
    )

    return EventUi(
        id = id,
        title = title,
        location = location,
        status = EventStatus.Confirmed,
        categories = null,
        start = Instant.parse(start),
        end = Instant.parse(end),
        colors = EventColors.from(color.toArgb()).toEventColorsUi(),
        attendees = Attendees(attendees, attendees.first()),
    )
}

private val week25 = WeekNumbering.ISO_8601.weekOf(LocalDate(2026, 6, 15))
private val week26 = WeekNumbering.ISO_8601.weekOf(LocalDate(2026, 6, 22))

private val fakeEventsByWeekAndDay: EventsByWeekAndDay = sortedMapOf(
    week25 to sortedMapOf(
        LocalDate(2026, 6, 15) to listOf(
            eventUi(
                id = "weekly-sync",
                title = "Weekly Sync",
                start = "2026-06-15T10:00:00Z",
                end = "2026-06-15T11:00:00Z",
                location = "Meeting Room A",
            ),
        ),
        LocalDate(2026, 6, 16) to listOf(
            eventUi(
                id = "design-review",
                title = "Design Review",
                start = "2026-06-16T15:00:00Z",
                end = "2026-06-16T16:00:00Z",
                location = "Meeting Room B",
                color = Color(0xFF9C27B0),
            ),
        ),
        LocalDate(2026, 6, 17) to listOf(
            eventUi(
                id = "team-lunch",
                title = "Team Lunch",
                start = "2026-06-17T12:30:00Z",
                end = "2026-06-17T13:30:00Z",
                location = "Ristorante Bella Italia",
                color = Color(0xFFE91E63),
            ),
        ),
        LocalDate(2026, 6, 20) to listOf(
            eventUi(
                id = "hiking-day",
                title = "Hiking Day",
                start = "2026-06-20T00:00:00Z",
                end = "2026-06-20T00:30:00Z",
                location = "Salève, France",
                color = Color(0xFF4CAF50),
            ),
        ),
    ),
    week26 to sortedMapOf(
        LocalDate(2026, 6, 22) to listOf(
            eventUi(
                id = "product-review",
                title = "Product Review",
                start = "2026-06-22T14:00:00Z",
                end = "2026-06-22T15:30:00Z",
                location = "Conf Room B / kMeet",
            ),
        ),
    ),
)
