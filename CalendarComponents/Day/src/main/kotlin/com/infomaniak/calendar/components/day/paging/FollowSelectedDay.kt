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
package com.infomaniak.calendar.components.day.paging

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import com.infomaniak.calendar.components.day.state.DayColumnsState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Keeps a row of days and the day selected outside of it in step, both ways: selecting a day brings
 * its page into view, and moving the row selects a day in view.
 *
 * Moving the row keeps the selection where it was among the days in view, so that turning a week
 * selects the same weekday of the next one rather than its first day.
 *
 * @param selectedDay The index of the selected day in the row.
 */
@Composable
internal fun FollowSelectedDay(
    stripState: LazyListState,
    columnsState: DayColumnsState,
    daysPerPage: Int,
    dayCount: Int,
    selectedDay: () -> Int,
    onVisibleDayChanged: (Int) -> Unit,
) {
    val currentSelectedDay by rememberUpdatedState(selectedDay)
    val currentOnVisibleDayChanged by rememberUpdatedState(onVisibleDayChanged)

    fun columnWidth() = columnsState.columnWidthPx(stripState.layoutInfo.viewportSize.width)

    LaunchedEffect(stripState, columnsState, daysPerPage, dayCount) {
        snapshotFlow { currentSelectedDay() }.collectLatest { day ->
            val columnWidth = columnWidth()
            if (columnWidth == 0) return@collectLatest

            val visibleDayCount = columnsState.visibleDayCount
            val position = stripState.firstVisibleDay(columnWidth)
            val target = positionRevealing(day, position, visibleDayCount, daysPerPage)
                .coerceIn(0f, max(0f, dayCount - visibleDayCount))
            if (target == position) return@collectLatest

            val index = floor(target).toInt()
            stripState.animateScrollToItem(index, scrollOffset = ((target - index) * columnWidth).roundToInt())
        }
    }

    LaunchedEffect(stripState, columnsState, dayCount) {
        // Where the selection sits among the days in view, last time it was in view.
        var selectionOffset = 0

        snapshotFlow {
            val columnWidth = columnWidth()
            // A jump scrolls the row as well, and a pinch moves it without scrolling it: neither is
            // the user moving to other days, and both are over once the row is at rest again.
            if (stripState.isScrollInProgress || columnsState.isPinching || columnWidth == 0) return@snapshotFlow null

            // The selection is read as well, so that picking another day in view moves the offset along.
            visibleDays(stripState.firstVisibleDay(columnWidth), columnsState.visibleDayCount) to currentSelectedDay()
        }.filterNotNull().collect { (visibleDays, selected) ->
            if (selected in visibleDays) {
                selectionOffset = selected - visibleDays.first
            } else {
                currentOnVisibleDayChanged((visibleDays.first + selectionOffset).coerceIn(visibleDays))
            }
        }
    }
}

/** The index of the first day of a row starting with [day] on its own page, as the row snaps to pages. */
internal fun pagePositionOf(day: Int, visibleDayCount: Float, daysPerPage: Int): Float {
    val pageStart = (day / daysPerPage * daysPerPage).toFloat()

    // Zoomed in, a page is wider than the viewport: the row then holds the part of it with the day.
    return day.toFloat().coerceIn(pageStart, max(pageStart, pageStart + daysPerPage - visibleDayCount))
}

/** Where to move a row at [position] to show [day] in full, staying where it is if it already does. */
internal fun positionRevealing(day: Int, position: Float, visibleDayCount: Float, daysPerPage: Int): Float {
    val isInView = day >= position - VISIBILITY_TOLERANCE && day + 1 <= position + visibleDayCount + VISIBILITY_TOLERANCE
    return if (isInView) position else pagePositionOf(day, visibleDayCount, daysPerPage)
}

/** The days a row at [position] shows for the most part, at least one of them. */
internal fun visibleDays(position: Float, visibleDayCount: Float): IntRange {
    val first = position.roundToInt()
    return first until max((position + visibleDayCount).roundToInt(), first + 1)
}

/** In days: makes up for the whole pixels a row is scrolled by, which rarely land exactly on a day. */
private const val VISIBILITY_TOLERANCE = 0.01f
