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
package com.infomaniak.calendar.ui.screen.month.pendingKmp

import com.infomaniak.calendar.components.month.model.MonthEventSegmentUi
import com.infomaniak.calendar.components.month.model.MonthWeekUi
import com.infomaniak.calendar.ui.model.OccurrenceEventSummaryUi
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth

/**
 * Packs the events of a week row into lanes, as iOS `MonthWeekLayout` does: an occurrence shown on consecutive days
 * becomes one segment over them, and each segment takes the lowest lane free on every column it covers.
 *
 * [eventsByDate] holds each day's events in display order; only the dates of [month] show theirs.
 */
internal fun packWeek(
    dates: List<LocalDate>,
    month: YearMonth,
    eventsByDate: Map<LocalDate, List<OccurrenceEventSummaryUi>>,
): MonthWeekUi {
    val columnEvents = dates.map { date -> if (date.yearMonth == month) eventsByDate[date].orEmpty() else emptyList() }
    val occupiedLanes = List(dates.size) { mutableSetOf<Int>() }

    val segments = runsOf(columnEvents).sortedWith(laneOrder).map { run ->
        val lane = generateSequence(0, Int::inc).first { lane -> run.columns.none { lane in occupiedLanes[it] } }
        run.columns.forEach { occupiedLanes[it] += lane }
        MonthEventSegmentUi(run.event, run.startColumn, run.span, lane)
    }

    return MonthWeekUi(dates, segments)
}

/** Each run of consecutive columns showing the same occurrence, in the order they start. */
private fun runsOf(columnEvents: List<List<OccurrenceEventSummaryUi>>): List<Run> {
    val runs = mutableListOf<Run>()
    var previousColumnRuns = emptyMap<OccurrenceId, Int>() // Indices in runs; a column without the occurrence ends its run.

    columnEvents.forEachIndexed { column, events ->
        previousColumnRuns = buildMap {
            events.forEach { event ->
                val previousIndex = previousColumnRuns[event.occurrenceId]
                if (previousIndex != null) runs[previousIndex] = runs[previousIndex].extended() else runs += Run(event, column)
                put(event.occurrenceId, previousIndex ?: runs.lastIndex)
            }
        }
    }

    return runs
}

/** Multi-day runs on top, earliest then longest first; the stable sort keeps the start order for the rest. */
private val laneOrder = compareByDescending<Run> { it.isMultiDay }.thenBy { it.startColumn }.thenByDescending { it.span }

private data class Run(val event: OccurrenceEventSummaryUi, val startColumn: Int, val span: Int = 1) {
    val columns: IntRange get() = startColumn until startColumn + span
    val isMultiDay: Boolean get() = span > 1

    fun extended(): Run = copy(span = span + 1)
}
