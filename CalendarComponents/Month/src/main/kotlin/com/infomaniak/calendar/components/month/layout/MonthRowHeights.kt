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
package com.infomaniak.calendar.components.month.layout

import androidx.compose.ui.unit.IntSize
import com.infomaniak.calendar.components.month.model.MonthWeekUi

/**
 * The rows of a page of [pageSize] showing [weeks], never shorter than a single lane needs. They share the height above
 * [bottomPadding], and the last one reaches under it, so the grid goes down to the page's edge behind what the host draws
 * there. Lanes must be counted from [MonthRowHeights.height], never from the extended last row.
 */
internal fun monthRowHeights(
    pageSize: IntSize,
    bottomPadding: Int,
    weeks: List<MonthWeekUi>,
    laneMetrics: LaneMetrics,
): MonthRowHeights {
    val rowsSize = IntSize(pageSize.width, pageSize.height - bottomPadding)

    fun rowHeights(height: Int, droppedPixels: Int): MonthRowHeights {
        val rowHeight = height.coerceAtLeast(laneMetrics.rowHeightFor(laneCount = 1))
        // Rows raised to a lane's height already overflow the page: there is nothing left to fill.
        val fillingPixels = if (rowHeight == height) droppedPixels else 0
        return MonthRowHeights(rowHeight, lastHeight = rowHeight + fillingPixels + bottomPadding)
    }

    return when (MonthGridSizing.of(rowsSize)) {
        // The last row takes the pixels the division drops, so the rows fill the height exactly.
        MonthGridSizing.Fill -> rowHeights(rowsSize.height / weeks.size, droppedPixels = rowsSize.height % weeks.size)
        MonthGridSizing.Square -> rowHeights(rowsSize.width / weeks.first().dates.size, droppedPixels = 0)
    }
}

/** How the rows of a month page get their height. */
private enum class MonthGridSizing {
    /** The rows share the page's height. */
    Fill,

    /** Each row is as tall as a column is wide: on a page wider than tall, sharing its height would leave no room for events. */
    Square;

    companion object {
        fun of(pageSize: IntSize): MonthGridSizing = if (pageSize.width > pageSize.height) Square else Fill
    }
}

/** The heights of a month page's rows, in pixels: [height] each, but [lastHeight] for the last one. */
internal class MonthRowHeights(val height: Int, val lastHeight: Int)
