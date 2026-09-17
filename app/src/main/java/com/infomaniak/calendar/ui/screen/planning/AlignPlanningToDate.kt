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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.infomaniak.calendar.components.planning.PlanningItemKey
import com.infomaniak.calendar.components.planning.PlanningRow
import com.infomaniak.calendar.ui.state.VisibleDayState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

/**
 * Applies a single initial alignment and turns explicit [VisibleDayState] jump requests into a
 * recentering followed by one scroll. Paging then owns normal anchor preservation across refreshes.
 */
@Composable
fun AlignPlanningToDate(
    lazyListState: LazyListState,
    planningRows: LazyPagingItems<PlanningRow>,
    visibleDayState: VisibleDayState,
    onJumpTo: (LocalDate) -> Boolean,
) {
    var initialAlignmentCompleted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(planningRows, visibleDayState) {
        if (!initialAlignmentCompleted) {
            planningRows.scrollToDate(lazyListState, visibleDayState.visibleDate, onJumpTo)
            initialAlignmentCompleted = true
        }

        for (date in visibleDayState.scrollCommand) {
            planningRows.scrollToDate(lazyListState, date, onJumpTo)
        }
    }
}

/**
 * Recenters the pager on [date] when needed, then scrolls to its row once the target page is ready.
 */
private suspend fun LazyPagingItems<PlanningRow>.scrollToDate(
    lazyListState: LazyListState,
    date: LocalDate,
    onJumpTo: (LocalDate) -> Boolean,
) {
    val rebuilt = onJumpTo(date)
    if (rebuilt) {
        val refreshState = snapshotFlow { loadState.refresh }
            .dropWhile { it !is LoadState.Loading }
            .first { it is LoadState.NotLoading || it is LoadState.Error }
        if (refreshState is LoadState.Error) return
    }

    val index = snapshotFlow { indexOfDate(date) }.first { it >= 0 }
    try {
        lazyListState.scrollToItem(index)
    } catch (_: CancellationException) {
        currentCoroutineContext().ensureActive()
    }
}

internal fun LazyPagingItems<PlanningRow>.indexOfDate(date: LocalDate): Int {
    return itemSnapshotList.indexOfFirst { (it?.key as? PlanningItemKey)?.date == date }
}
