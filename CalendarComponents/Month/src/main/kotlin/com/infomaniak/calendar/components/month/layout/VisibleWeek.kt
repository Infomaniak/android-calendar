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
package com.infomaniak.calendar.components.month.layout

import androidx.compose.runtime.Immutable
import com.infomaniak.calendar.components.month.model.MonthEventSegmentUi
import com.infomaniak.calendar.components.month.model.MonthWeekUi

/**
 * Keeps the segments fitting in [laneCount] lanes. The last lane shows an event only if none of the days it covers has
 * more events than lanes: otherwise that lane holds the hidden events indicator of those days, and the event is hidden
 * whole, in every day it covers.
 */
internal fun MonthWeekUi.visibleIn(laneCount: Int): VisibleWeek {
    val usedLanes = IntArray(dates.size)
    segments.forEach { segment -> segment.columns.forEach { usedLanes[it] = maxOf(usedLanes[it], segment.lane + 1) } }

    val lastLane = laneCount - 1
    val (visible, hidden) = segments.partition { segment ->
        segment.lane < lastLane || segment.lane == lastLane && segment.columns.none { usedLanes[it] > laneCount }
    }

    val hasHiddenEvents = BooleanArray(dates.size)
    hidden.forEach { segment -> segment.columns.forEach { hasHiddenEvents[it] = true } }

    return VisibleWeek(visible, hasHiddenEvents.asList(), indicatorLane = lastLane)
}

/**
 * What a week shows once its lanes are limited: the [segments] drawn, and for each column whether segments are hidden
 * over it, which [indicatorLane] then indicates.
 */
@Immutable
internal data class VisibleWeek(
    val segments: List<MonthEventSegmentUi>,
    val hasHiddenEvents: List<Boolean>,
    val indicatorLane: Int,
)
