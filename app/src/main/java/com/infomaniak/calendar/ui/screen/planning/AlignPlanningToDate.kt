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

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.paging.compose.LazyPagingItems
import com.infomaniak.calendar.components.planning.PlanningItemKey
import com.infomaniak.calendar.components.planning.PlanningRow
import com.infomaniak.calendar.ui.state.VisibleDayState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * Keeps the planning **pinned** to a target date and turns [VisibleDayState] jump requests into
 * recentering + pinning. Replaces the old "center once" + "process jumps" pair.
 *
 * A one-shot `scrollToItem` doesn't hold: prefetch prepends and, above all, the initial events sync
 * refreshes the pager (replacing placeholder rows whose keys vanish), so the position slips onto a date
 * of the previous week. Instead we **re-scroll to the pinned date on every content shift** until the
 * user drags (which releases the pin); an explicit jump re-pins on its date.
 *
 * This converges because the window stays centered (a leading week above the target), keeping the
 * target away from the leading edge so prefetch can't prepend endlessly.
 */
@Composable
fun AlignPlanningToDate(
    lazyListState: LazyListState,
    planningRows: LazyPagingItems<PlanningRow>,
    visibleDayState: VisibleDayState,
    onJumpTo: (LocalDate) -> Boolean,
) {
    var hasCentered by rememberSaveable { mutableStateOf(false) }

    // The date the planning stays glued to; `null` once the user drags (pin released). Kept separate
    // from `visibleDayState.visibleDate`: that one is the *observed* top (it follows the drift), while
    // this is the *desired* anchor we force back to — reusing it would loop (target = the drift itself).
    val pinnedDate = remember { mutableStateOf(if (hasCentered) null else visibleDayState.visibleDate) }

    LaunchedEffect(planningRows, visibleDayState) {
        // Initial centering, once per process. onJumpTo rebuilds the pager only if the restored date
        // isn't today (the pager starts centered on today); it's a no-op on a fresh launch.
        if (!hasCentered) {
            onJumpTo(visibleDayState.visibleDate)
            hasCentered = true
        }

        // Release the pin the moment the user drags the list — from then on they scroll freely.
        launch {
            lazyListState.interactionSource.interactions
                .filterIsInstance<DragInteraction.Start>()
                .collect { pinnedDate.value = null }
        }

        // Explicit jumps recenter the pager and re-pin onto the requested date.
        launch {
            for (date in visibleDayState.scrollCommand) {
                pinnedDate.value = date
                onJumpTo(date)
            }
        }

        // Keep the list glued to the pinned date across every content shift (prepend, sync refresh).
        launch { planningRows.keepPinnedToDate(lazyListState, pinnedDate) }
    }
}

/**
 * Re-scrolls [lazyListState] so the row for [pinnedDate]'s value stays at the top on every content
 * shift, until the pin is released (`null`). [indexOfDate] returns -1 while the date isn't loaded yet
 * (e.g. mid-refresh of a far jump), where we simply wait for the fresh window.
 */
private suspend fun LazyPagingItems<PlanningRow>.keepPinnedToDate(
    lazyListState: LazyListState,
    pinnedDate: State<LocalDate?>,
) {
    snapshotFlow {
        pinnedDate.value?.let { date ->
            val index = indexOfDate(date)
            val atTop = lazyListState.firstVisibleItemIndex == index && lazyListState.firstVisibleItemScrollOffset == 0
            PinAlignment(index = index, atTop = atTop)
        }
    }.collect { alignment ->
        if (alignment != null && alignment.index >= 0 && !alignment.atTop) {
            try {
                lazyListState.scrollToItem(alignment.index)
            } catch (_: CancellationException) {
                // Ignore the scroll being preempted by a user gesture, but propagate a real cancellation.
                currentCoroutineContext().ensureActive()
            }
        }
    }
}

private data class PinAlignment(val index: Int, val atTop: Boolean)

internal fun LazyPagingItems<PlanningRow>.indexOfDate(date: LocalDate): Int {
    return itemSnapshotList.indexOfFirst { (it?.key as? PlanningItemKey)?.date == date }
}












