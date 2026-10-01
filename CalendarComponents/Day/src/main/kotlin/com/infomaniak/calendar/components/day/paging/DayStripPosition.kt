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
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Positions in a row of days are counted in days rather than in pixels: unlike pixels, days do not
 * change when a zoom widens the columns, so a position survives a zoom as it is.
 */

/**
 * Where a row of days of [columnWidth] pixels each is scrolled to: the index of its first visible
 * day, plus the part of that day already scrolled out of view.
 */
internal fun LazyListState.firstVisibleDay(columnWidth: Int): Float {
    return firstVisibleItemIndex + firstVisibleItemScrollOffset / columnWidth.toFloat()
}

/**
 * Scrolls the row so that its [firstVisibleDay] becomes [day] on its next measure, cancelling any
 * scroll in progress. Being applied at the measure, it already lays the row out at the column width
 * of a zoom made in the meantime.
 */
internal fun LazyListState.requestFirstVisibleDay(day: Float, columnWidth: Int) {
    val position = day.coerceAtLeast(0f)
    val index = floor(position).toInt()

    requestScrollToItem(index, scrollOffset = ((position - index) * columnWidth).roundToInt())
}

/** Where the day at [index] starts in the viewport, or null while it is out of view. */
internal fun LazyListState.viewportOffsetOf(index: Int): Int? {
    return layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.offset
}
