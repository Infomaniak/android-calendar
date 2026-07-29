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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first

/** How close the selection may get to a bound before the range is grown on that side. */
private const val GROWTH_TRIGGER_MARGINS = 1

/** How far past the selection a bound is pushed when growing, so this only happens once per margin. */
private const val GROWTH_TARGET_MARGINS = 2

/**
 * Keeps a paged calendar showing the current selection, and keeps its page range wide enough for
 * the pager to always have somewhere to go.
 *
 * The range matters because a calendar state resolves a page to a list index and gives up silently
 * when the page falls outside its bounds: an out-of-range scroll is not an error, it is a no-op. A
 * pager that has reached its bounds therefore looks frozen while the rest of the screen keeps
 * following the selection.
 *
 * Growing forward only appends pages, which leaves the existing indices pointing at the same dates,
 * so it can happen at any time. Growing backward prepends pages and shifts every index by the
 * number added, silently moving the pager onto another page, so it waits for the pager to be still
 * and puts the visible page back afterwards.
 *
 * @param scrollableState the layout being scrolled.
 * @param selectedPage the page the rest of the screen is on.
 * @param displayedPage the page currently on screen.
 * @param pageRange the pages the layout currently holds.
 * @param setPageRange widens [pageRange]. Only ever called with a range containing the current one.
 * @param shiftByMargins a page shifted by a signed number of margins, the unit this grows by.
 * @param scrollToPage moves the layout to the given page, animated or not.
 */
@Composable
internal fun <P : Comparable<P>> FollowExternalSelection(
    scrollableState: ScrollableState,
    selectedPage: () -> P,
    displayedPage: () -> P,
    pageRange: () -> ClosedRange<P>,
    setPageRange: (ClosedRange<P>) -> Unit,
    shiftByMargins: (page: P, margins: Int) -> P,
    scrollToPage: suspend (page: P, animate: Boolean) -> Unit,
) {
    val currentSelectedPage by rememberUpdatedState(selectedPage)
    val currentDisplayedPage by rememberUpdatedState(displayedPage)
    val currentPageRange by rememberUpdatedState(pageRange)
    val currentSetPageRange by rememberUpdatedState(setPageRange)
    val currentShiftByMargins by rememberUpdatedState(shiftByMargins)
    val currentScrollToPage by rememberUpdatedState(scrollToPage)

    LaunchedEffect(scrollableState) {
        snapshotFlow { currentSelectedPage() }.collect { page ->
            val range = currentPageRange()

            if (currentShiftByMargins(page, GROWTH_TRIGGER_MARGINS) > range.endInclusive) {
                currentSetPageRange(range.start..currentShiftByMargins(page, GROWTH_TARGET_MARGINS))
            }

            if (currentShiftByMargins(page, -GROWTH_TRIGGER_MARGINS) < range.start) {
                snapshotFlow { scrollableState.isScrollInProgress }.first { !it }

                val settledRange = currentPageRange()
                val wantedStart = currentShiftByMargins(page, -GROWTH_TARGET_MARGINS)
                if (wantedStart < settledRange.start) {
                    // Read what is on screen before the indices move under it.
                    val anchor = currentDisplayedPage()
                    currentSetPageRange(wantedStart..settledRange.endInclusive)
                    reanchorOn(anchor, currentScrollToPage)
                }
            }
        }
    }

    LaunchedEffect(scrollableState) {
        snapshotFlow { currentSelectedPage() }.collectLatest { page ->
            // Waiting rather than skipping: scrolling now would fight an ongoing swipe, and
            // dropping the request would lose the jump entirely.
            snapshotFlow { scrollableState.isScrollInProgress }.first { !it }
            // A page the layout does not hold yet cannot be scrolled to, and the effect above may
            // still be catching up with a distant jump.
            snapshotFlow { page in currentPageRange() }.first { it }

            if (page != currentDisplayedPage()) currentScrollToPage(page, true)
        }
    }
}

/**
 * Puts [anchor] back on screen after the indices have shifted under it.
 *
 * A gesture starting in that same frame takes the scroll over and cancels this, which is survivable:
 * the pager follows the finger from a page off by the growth, and the next release corrects it. The
 * alternative, letting the cancellation through, would tear down the effect for good.
 */
private suspend fun <P> reanchorOn(anchor: P, scrollToPage: suspend (P, Boolean) -> Unit) {
    try {
        scrollToPage(anchor, false)
    } catch (_: CancellationException) {
        currentCoroutineContext().ensureActive()
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
