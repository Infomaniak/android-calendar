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
package com.infomaniak.calendar.components.calendar.modifier

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import com.infomaniak.calendar.components.calendar.component.CalendarHeaderState
import com.infomaniak.calendar.components.calendar.component.CalendarPager
import com.infomaniak.calendar.components.calendar.component.followSelection
import com.infomaniak.calendar.components.calendar.component.growRangeAround
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate

/**
 * Keeps the pager showing the selected date when something other than a swipe changes it, and keeps
 * its page range wide enough that it always has somewhere to go.
 *
 * The range matters because a calendar resolves a page to a list index and gives up silently when
 * the page falls outside its bounds: an out-of-range scroll is not an error, it is a no-op. A pager
 * that has reached its bounds therefore looks frozen while the rest of the screen keeps following
 * the selection.
 */
@Composable
internal fun FollowExternalSelection(pager: CalendarPager, selectedDate: () -> LocalDate) {
    val currentSelectedDate by rememberUpdatedState(selectedDate)
    val selectedPage = remember(pager) { snapshotFlow { pager.pageOf(currentSelectedDate()) } }

    LaunchedEffect(pager) { selectedPage.collect { pager.growRangeAround(it) } }
    LaunchedEffect(pager) { selectedPage.collectLatest { pager.followSelection(it) } }
}

@Composable
internal fun SyncHeaderOffset(
    scrollableState: ScrollableState,
    headerState: CalendarHeaderState,
    layoutInfo: () -> LazyListLayoutInfo,
    setOffsetSource: (() -> Float) -> Unit,
) {
    LaunchedEffect(scrollableState, headerState) {
        setOffsetSource { layoutInfo().dayColumnsOffset() }
    }
}

/**
 * How far the day columns have scrolled within the current page, which a header drawn outside the
 * pager has to follow to stay aligned with them.
 *
 * Derived from the item index rather than from its offset alone, which jumps back to zero whenever
 * the leading item changes mid-swipe, and wrapped to a single page so a looping header can chain
 * two copies of itself.
 */
private fun LazyListLayoutInfo.dayColumnsOffset(): Float {
    val pageWidth = viewportSize.width
    if (pageWidth <= 0) return 0f
    val firstVisibleItem = visibleItemsInfo.firstOrNull() ?: return 0f
    val scrolledDistance = firstVisibleItem.index.toLong() * pageWidth - firstVisibleItem.offset

    return -(scrolledDistance.toFloat().mod(pageWidth.toFloat()))
}
