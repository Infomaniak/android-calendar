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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.infomaniak.calendar.components.planning.PlanningRow
import com.infomaniak.calendar.ui.state.VisibleDayState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDate

/**
 * Applies a single initial alignment and turns explicit [VisibleDayState] jump requests into a
 * recentering followed by one scroll. Automatic page changes preserve the current row and its pixel offset instead.
 */
@Composable
internal fun AlignPlanningToDate(
    lazyListState: LazyListState,
    planningRows: LazyPagingItems<PlanningRow>,
    visibleDayState: VisibleDayState,
    onJumpTo: (LocalDate) -> Long,
    onNavigationFinished: (Long) -> Unit,
    onInitialAlignmentCompleted: () -> Unit,
) {
    var initialAlignmentCompleted by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }
    val snapshot = planningRows.itemSnapshotList
    val keys = remember(snapshot) { snapshot.items.map { it.key } }

    PreservePlanningScrollPosition(
        keys = keys,
        lazyListState = lazyListState,
        enabled = initialAlignmentCompleted && !isNavigating,
    )

    LaunchedEffect(planningRows, visibleDayState) {
        visibleDayState.scrollCommand.receiveAsFlow()
            .map { DateAlignment(it, forceRecenter = true) }
            .onStart {
                if (!initialAlignmentCompleted) emit(DateAlignment(visibleDayState.visibleDate, forceRecenter = false))
            }
            .collectLatest { request ->
                isNavigating = true
                try {
                    planningRows.scrollToDate(
                        lazyListState = lazyListState,
                        date = request.date,
                        forceRecenter = request.forceRecenter,
                        onJumpTo = onJumpTo,
                        onNavigationFinished = onNavigationFinished,
                    )
                    if (!initialAlignmentCompleted) {
                        initialAlignmentCompleted = true
                        onInitialAlignmentCompleted()
                    }
                } finally {
                    isNavigating = false
                }
            }
    }
}

private data class DateAlignment(val date: LocalDate, val forceRecenter: Boolean)

/**
 * Recenters the pager when an explicit navigation requests it or the target is no longer loaded, then scrolls to its row.
 */
private suspend fun LazyPagingItems<PlanningRow>.scrollToDate(
    lazyListState: LazyListState,
    date: LocalDate,
    forceRecenter: Boolean,
    onJumpTo: (LocalDate) -> Long,
    onNavigationFinished: (Long) -> Unit,
) {
    val currentIndex = indexOfDate(date)
    val rebuilt = forceRecenter || currentIndex < 0
    val navigationGeneration = if (rebuilt) onJumpTo(date) else null

    try {
        if (rebuilt) {
            // Keep the target pending on failure: the error UI offers retry, and a newer jump cancels this wait.
            snapshotFlow { loadState.refresh }
                .dropWhile { it !is LoadState.Loading }
                .first { it is LoadState.NotLoading }
        }

        val index = if (rebuilt) {
            snapshotFlow { indexOfDate(date) }.first { it >= 0 }
        } else {
            currentIndex
        }
        try {
            lazyListState.scrollToItem(index)
        } catch (_: CancellationException) {
            currentCoroutineContext().ensureActive()
        }
    } finally {
        navigationGeneration?.let(onNavigationFinished)
    }
}

internal fun LazyPagingItems<PlanningRow>.indexOfDate(date: LocalDate): Int {
    return itemSnapshotList.indexOfFirst { it?.key?.date == date }
}
