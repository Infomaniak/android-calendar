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
package com.infomaniak.calendar.ui.screen.planning

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.infomaniak.calendar.components.planning.PlanningItemKey
import com.infomaniak.calendar.components.planning.PlanningRow
import com.infomaniak.calendar.ui.state.VisibleDayState
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

/**
 * Turns [VisibleDayState.scrollCommand] jump requests into a scroll to that date (see [jumpToDate]).
 */
@Composable
fun ProcessJumpRequests(
    lazyListState: LazyListState,
    planningRows: LazyPagingItems<PlanningRow>,
    visibleDayState: VisibleDayState,
    onJumpTo: (LocalDate) -> Boolean,
) {
    LaunchedEffect(planningRows, visibleDayState) {
        for (date in visibleDayState.scrollCommand) {
            planningRows.jumpToDate(lazyListState, date, onJumpTo)
        }
    }
}

/**
 * Recenters the pager on [date] via [onJumpTo], then top-aligns its row.
 *
 * We always recenter rather than scrolling within the current window: scrolling to a row that sits
 * near the leading edge of a scrolled window makes Paging prepend, which shifts the position and sent
 * the jump drifting far into the past. Recentering puts a full leading week above the target instead,
 * so — with a small `prefetchDistance` — no prepend fires right after the jump.
 *
 * When [onJumpTo] actually rebuilds the pager (the center changed), we wait for that new generation's
 * refresh to complete before resolving the index, so we align against the fresh centered window and
 * never the outgoing (scrolled) one. When it's a no-op (already centered), we scroll straight away.
 */
internal suspend fun LazyPagingItems<PlanningRow>.jumpToDate(
    lazyListState: LazyListState,
    date: LocalDate,
    onJumpTo: (LocalDate) -> Boolean,
) {
    val rebuilt = onJumpTo(date)

    if (rebuilt) {
        // Wait for the rebuilt pager's refresh to begin (Loading) then finish (NotLoading).
        snapshotFlow { loadState.refresh }
            .dropWhile { it !is LoadState.Loading }
            .first { it is LoadState.NotLoading }
    }

    val index = snapshotFlow { indexOfDate(date) }.first { it >= 0 }

    // Swallow the CancellationException thrown when a user gesture interrupts the programmatic scroll.
    runCatching { lazyListState.scrollToItem(index) }
}

internal fun LazyPagingItems<PlanningRow>.indexOfDate(date: LocalDate): Int {
    return itemSnapshotList.indexOfFirst { (it?.key as? PlanningItemKey)?.date == date }
}

