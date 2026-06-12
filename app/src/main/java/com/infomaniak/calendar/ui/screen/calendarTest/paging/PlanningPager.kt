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
 * Owns the visible time [range] and grows it for infinite scroll, based on scroll position.
 *
 * Pure logic (no coroutines): callers observe [range] and call [onScroll] / [onContentLoaded].
 *
 * Anti-stall design: [onScroll] re-evaluates on every update (no transition filtering), so paging
 * always makes progress even if a chunk doesn't leave the prefetch zone; a per-direction guard
 * (released by [onContentLoaded]) prevents spamming while a chunk is in flight.
 *
 * Invariant: [chunk] must add more week separators than [prefetch] can reach (120 days ≈ 17 weeks
 * > a typical viewport / [minPrefetchItems]), otherwise paging over event-less periods could stall.
 */
@OptIn(ExperimentalTime::class)
internal class PlanningPager(
    private val chunk: Duration = 120.days,
    private val minPrefetchItems: Int = 8,
    private val timeZone: TimeZone = TimeZone.UTC, // TODO: Timezones are not handled yet.
) {

    private val _range = MutableStateFlow(initialRange())
    val range: StateFlow<PlanningRange> = _range.asStateFlow()

    // One paging step per direction in flight at a time; released by onContentLoaded().
    private var isLoadingPast = false
    private var isLoadingFuture = false

    fun onScroll(info: ScrollInfo) {
        if (info.totalItemsCount <= 0) return

        val prefetch = info.prefetch()
        val reachedStart = info.firstVisibleIndex <= prefetch
        val reachedEnd = info.lastVisibleIndex >= info.totalItemsCount - 1 - prefetch

        if (reachedStart && !isLoadingPast) {
            isLoadingPast = true
            _range.update { current -> current.copy(start = current.start - chunk) }
        }
        if (reachedEnd && !isLoadingFuture) {
            isLoadingFuture = true
            _range.update { current -> current.copy(end = current.end + chunk) }
        }
    }

    /** A new page finished loading: allow the next paging step in each direction. */
    fun onContentLoaded() {
        isLoadingPast = false
        isLoadingFuture = false
    }

    // We can only reason in item counts (not-yet-composed items aren't measured, so an anticipatory
    // pixel distance is impossible — same reason paging libs use an item-based prefetchDistance).
    // To stay robust to screen size AND items of varying heights, prefetch the larger of the current
    // viewport (adapts now) and a fixed floor (covers many small items coming next).
    private fun ScrollInfo.prefetch(): Int = maxOf(lastVisibleIndex - firstVisibleIndex + 1, minPrefetchItems)

    private fun initialRange(): PlanningRange {
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val start = today.atStartOfDayIn(timeZone)
        return PlanningRange(start = start, end = start + chunk)
    }
}

