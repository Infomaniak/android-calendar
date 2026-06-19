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
package com.infomaniak.calendar.components.planning.preview

import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.models.YearWeek
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Instant

internal val previewWeekEvents: Map<YearWeek, Map<LocalDate, List<EventUi>>> by lazy {
    val timeZone = TimeZone.currentSystemDefault()
    val today = Clock.System.todayIn(timeZone)
    val pastDay = today.minus(5, DateTimeUnit.DAY)
    val futureDay = today.plus(5, DateTimeUnit.DAY)

    fun instantAt(date: LocalDate, hour: Int): Instant {
        return LocalDateTime(date.year, date.month.ordinal, date.day, hour, 0).toInstant(timeZone)
    }

    fun event(date: LocalDate, hour: Int, title: String, location: String? = null, color: Int = 0xFF4285F4.toInt()): EventUi {
        return EventUi(
            id = "$date-$hour",
            title = title,
            location = location,
            categories = null,
            start = instantAt(date, hour),
            end = instantAt(date, hour + 1),
            color = color,
        )
    }

    listOf(
        pastDay to listOf(
            event(pastDay, 10, "Team retrospective", "Conference room B"),
        ),
        today to listOf(
            event(today, 9, "Morning standup", "kMeet", 0xFF0F9D58.toInt()),
            event(today, 14, "Design review", "Japan room", 0xFFDB4437.toInt()),
        ),
        futureDay to listOf(
            event(futureDay, 11, "Sprint planning", "Hard rock room"),
        ),
    ).groupForPlanning()
}

private fun List<Pair<LocalDate, List<EventUi>>>.groupForPlanning(): Map<YearWeek, Map<LocalDate, List<EventUi>>> =
    groupBy { (date, _) -> WeekNumbering.ISO_8601.weekOf(date) }
        .mapValues { (_, pairs) -> pairs.associate { (date, events) -> date to events } }
