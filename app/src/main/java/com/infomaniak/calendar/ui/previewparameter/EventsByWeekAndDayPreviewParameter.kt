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
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.ui.screen.planning.EventsByWeekAndDay
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
    color: Int = 0xFF2196F3.toInt(),
) = EventUi(
    id = id,
    title = title,
    location = location,
    categories = null,
    start = Instant.parse(start),
    end = Instant.parse(end),
    color = color,
)

private val week25 = WeekNumbering.ISO_8601.weekOf(LocalDate(2026, 6, 15))
private val week26 = WeekNumbering.ISO_8601.weekOf(LocalDate(2026, 6, 22))

private val fakeEventsByWeekAndDay: EventsByWeekAndDay = sortedMapOf(
    week25 to sortedMapOf(
        LocalDate(2026, 6, 15) to listOf(eventUi("weekly-sync",   "Weekly Sync",   "2026-06-15T10:00:00Z", "2026-06-15T11:00:00Z", "Meeting Room A")),
        LocalDate(2026, 6, 16) to listOf(eventUi("design-review", "Design Review", "2026-06-16T15:00:00Z", "2026-06-16T16:00:00Z", "Meeting Room B", 0xFF9C27B0.toInt())),
        LocalDate(2026, 6, 17) to listOf(eventUi("team-lunch",    "Team Lunch",    "2026-06-17T12:30:00Z", "2026-06-17T13:30:00Z", "Ristorante Bella Italia", 0xFFE91E63.toInt())),
        LocalDate(2026, 6, 20) to listOf(eventUi("hiking-day",    "Hiking Day",    "2026-06-20T00:00:00Z", "2026-06-20T00:30:00Z", "Salève, France", 0xFF4CAF50.toInt())),
    ),
    week26 to sortedMapOf(
        LocalDate(2026, 6, 22) to listOf(eventUi("product-review", "Product Review", "2026-06-22T14:00:00Z", "2026-06-22T15:30:00Z", "Conf Room B / kMeet", 0xFFFF9800.toInt())),
    ),
)
