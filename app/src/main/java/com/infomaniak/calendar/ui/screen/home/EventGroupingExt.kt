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

import com.infomaniak.calendar.components.models.EventUi
import com.infomaniak.calendar.components.models.WeekNumbering
import com.infomaniak.calendar.components.models.YearWeek
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.SortedMap
import kotlin.time.ExperimentalTime

/**
 * Events grouped by week, then by day.
 *
 * The outer map is keyed by [YearWeek] and the inner map by the day's [LocalDate], both sorted
 * ascending so the result can be consumed directly by a calendar UI. Days are keyed by full date
 * (not day-of-month) so a week that straddles two months stays in chronological order.
 */
typealias EventsByWeekAndDay = SortedMap<YearWeek, SortedMap<LocalDate, List<EventUi>>>

/**
 * Groups events by the [week][YearWeek] they fall in and then by their day.
 *
 * Events without a [start][com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming.start]
 * instant are ignored since they cannot be placed on a calendar day.
 *
 * Instants are bucketed using [timeZone] (the device's current timezone by default), and weeks are
 * resolved using [weekNumbering] (ISO-8601 by default).
 *
 * The nested sorted structure is filled in a single pass: each event is placed directly into its week
 * and day bucket, with no intermediate collections allocated along the way.
 */
@OptIn(ExperimentalTime::class)
fun List<Event>.groupByWeekAndDay(
    weekNumbering: WeekNumbering = WeekNumbering.ISO_8601,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): EventsByWeekAndDay {
    val result = sortedMapOf<YearWeek, SortedMap<LocalDate, MutableList<EventUi>>>()

    for (event in this) {
        val date = (event.timing as? EventTiming.Timed)?.start?.toLocalDateTime(timeZone)?.date ?: continue // TODO: Handle AllDay
        result
            .getOrPut(weekNumbering.weekOf(date)) { sortedMapOf() }
            .getOrPut(date) { mutableListOf() }
            .add(event.toEventUi() ?: continue)
    }

    @Suppress("UNCHECKED_CAST") // Makes the exposed list non-mutable
    return result as EventsByWeekAndDay
}

private fun Event.toEventUi(): EventUi? {
    val start = (timing as? EventTiming.Timed)?.start ?: return null // TODO: Handle AllDay
    val end = (timing as? EventTiming.Timed)?.resolvedEnd() ?: return null // TODO: Handle AllDay
    return EventUi(
        id = id.url,
        title = title,
        location = location,
        categories = categories,
        start = start,
        end = end,
        color = color,
    )
}
