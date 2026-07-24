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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import com.infomaniak.calendar.components.calendar.component.CalendarHeaderState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first

@Composable
internal fun <P> FollowExternalSelection(
    scrollableState: ScrollableState,
    selectedPage: () -> P,
    displayedPage: () -> P,
    animateScrollToPage: suspend (P) -> Unit,
) {
    val currentDisplayedPage by rememberUpdatedState(displayedPage)
    val currentAnimateScrollToPage by rememberUpdatedState(animateScrollToPage)

    LaunchedEffect(scrollableState) {
        snapshotFlow { selectedPage() }.collectLatest { page ->
            // Waiting rather than skipping: scrolling now would fight an ongoing swipe, and
            // dropping the request would lose the jump entirely.
            snapshotFlow { scrollableState.isScrollInProgress }.first { !it }
            if (page != currentDisplayedPage()) currentAnimateScrollToPage(page)
        }
    }
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
