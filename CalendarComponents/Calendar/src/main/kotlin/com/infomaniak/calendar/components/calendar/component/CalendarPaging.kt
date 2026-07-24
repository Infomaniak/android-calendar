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
package com.infomaniak.calendar.components.calendar.component

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first

/**
 * Logic shared by [ExpandedCalendar] and [CollapsedCalendar]. They wrap two different library
 * composables (`HorizontalCalendar` / `WeekCalendar`) over two different states, but drive them the
 * same way: the pieces that must stay identical between the two live here so a change is made once
 * and can never drift.
 */

/** Offset in px of the leading visible item, used by [pagedSwipe] to re-align pages after settling. */
internal fun LazyListLayoutInfo.firstVisibleItemOffset(): Int = visibleItemsInfo.firstOrNull()?.offset ?: 0

/**
 * Absolute scroll position of the pager wrapped to a single page, in px, within `[-pageWidth, 0]`.
 *
 * Computing it from the leading item's index (rather than its raw `offset`) keeps it continuous when
 * that item changes mid-swipe: `offset` alone jumps back to ~0 and would make the overlay header
 * snap sideways by a full page. The result is wrapped to one page because the header renders two
 * copies chasing each other and expects an offset in that range.
 */
internal fun LazyListLayoutInfo.pagedHeaderOffset(): Float {
    val pageWidth = viewportSize.width
    if (pageWidth <= 0) return 0f
    val info = visibleItemsInfo.firstOrNull() ?: return 0f
    val scrolled = info.index.toLong() * pageWidth - info.offset
    return -(scrolled.toFloat().mod(pageWidth.toFloat()))
}

/**
 * Follows selection changes coming from outside the calendar (day tap, jump to today, deep link) and
 * scrolls the pager to the matching page.
 *
 * @param state the calendar's scrollable state, keyed so the effect restarts if the calendar swaps.
 * @param selectedPage the page the current selection belongs to.
 * @param currentPage the page currently displayed.
 * @param animateScrollToPage animates the pager to the given page.
 */
@Composable
internal fun <P> FollowExternalSelection(
    state: ScrollableState,
    selectedPage: () -> P,
    currentPage: () -> P,
    animateScrollToPage: suspend (P) -> Unit,
) {
    LaunchedEffect(state) {
        snapshotFlow { selectedPage() }.collectLatest { page ->
            // Wait for any ongoing gesture or settle rather than dropping the request: scrolling now
            // would fight the swipe animation, and skipping would lose the jump entirely (e.g.
            // tapping "today" right after a swipe).
            snapshotFlow { state.isScrollInProgress }.first { !it }
            if (page != currentPage()) animateScrollToPage(page)
        }
    }
}

/**
 * Installs the pager's horizontal offset into [headerState] so [ExpandableCalendar]'s overlay header
 * can translate along with the day columns.
 *
 * The source is pull-based: [headerState] samples it during the draw phase, so the header stays in
 * sync with the columns frame for frame with no one-frame lag.
 *
 * @param state keyed so the source is reinstalled if the calendar swaps.
 * @param headerState the shared header state to feed.
 * @param layoutInfo the pager's current layout info, read lazily at draw time.
 * @param setSource the header slot to fill (expanded or collapsed).
 */
@Composable
internal fun SyncHeaderOffset(
    state: ScrollableState,
    headerState: CalendarHeaderState,
    layoutInfo: () -> LazyListLayoutInfo,
    setSource: (() -> Float) -> Unit,
) {
    LaunchedEffect(state, headerState) {
        setSource { layoutInfo().pagedHeaderOffset() }
    }
}
