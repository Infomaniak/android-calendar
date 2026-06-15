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

import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.SortedMap
import kotlin.time.ExperimentalTime

/**
 * Events grouped by month-year, then by day of the month.
 *
 * The outer map is keyed by [YearMonth] and the inner map by day-of-month (1..31), both sorted
 * ascending so the result can be consumed directly by a calendar UI.
 */
typealias EventsByMonthAndDay = SortedMap<YearMonth, SortedMap<Int, List<Event>>>

/**
 * Groups events by their month-year and then by their day of the month.
 *
 * Events without a [start][com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming.start]
 * instant are ignored since they cannot be placed on a calendar day.
 *
 * Instants are bucketed using the device's current timezone.
 *
 * The nested sorted structure is filled in a single pass: each event is placed directly into its
 * month and day bucket, with no intermediate collections allocated along the way.
 */
@OptIn(ExperimentalTime::class)
fun List<Event>.groupByMonthAndDay(): EventsByMonthAndDay {
    val timeZone = TimeZone.currentSystemDefault()
    val result = sortedMapOf<YearMonth, SortedMap<Int, MutableList<Event>>>()

    for (event in this) {
        val date = (event.timing as? EventTiming.Timed)?.start?.toLocalDateTime(timeZone)?.date ?: continue // TODO: Handle AllDay
        result
            .getOrPut(YearMonth(date.year, date.month.ordinal)) { sortedMapOf() }
            .getOrPut(date.day) { mutableListOf() }
            .add(event)
    }

    @Suppress("UNCHECKED_CAST") // Makes the exposed list non-mutable
    return result as EventsByMonthAndDay
}
