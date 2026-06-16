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
package com.infomaniak.calendar.ui.screen.calendarTest.paging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal data class PlanningRange(val start: Instant, val end: Instant)

/**
 * Owns the visible time [range] and grows it for infinite scroll.
 *
 * Each direction is gated by an armed latch that re-arms only once the viewport leaves the edge's
 * prefetch zone, so paging reacts to scrolling rather than to dataset updates re-emitting a parked
 * position. A [chunk] always adds more items than [ScrollInfo.prefetch] spans, so a page leaves the
 * zone and re-arms the next one.
 */
@OptIn(ExperimentalTime::class)
internal class PlanningPager(
    private val chunk: Duration = 120.days,
    private val minPrefetchItems: Int = 8,
    private val timeZone: TimeZone = TimeZone.UTC, // TODO: Timezones are not handled yet.
) {

    private val _range = MutableStateFlow(initialRange())
    val range: StateFlow<PlanningRange> = _range.asStateFlow()

    private var pastArmed = true
    private var futureArmed = true

    fun onScroll(info: ScrollInfo) {
        if (info.totalItemsCount <= 0) return

        val prefetch = info.prefetch()
        val reachedStart = info.firstVisibleIndex <= prefetch
        val reachedEnd = info.lastVisibleIndex >= info.totalItemsCount - 1 - prefetch

        if (!reachedStart) pastArmed = true
        if (!reachedEnd) futureArmed = true

        if (reachedStart && pastArmed) {
            pastArmed = false
            _range.update { current -> current.copy(start = current.start - chunk) }
        }
        if (reachedEnd && futureArmed) {
            futureArmed = false
            _range.update { current -> current.copy(end = current.end + chunk) }
        }
    }

    private fun ScrollInfo.prefetch(): Int = maxOf(lastVisibleIndex - firstVisibleIndex + 1, minPrefetchItems)

    private fun initialRange(): PlanningRange {
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val start = today.atStartOfDayIn(timeZone)
        return PlanningRange(start = start, end = start + chunk)
    }
}

