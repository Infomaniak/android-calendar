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
package com.infomaniak.calendar.components.day.zoom

import androidx.compose.foundation.lazy.LazyListState
import com.infomaniak.calendar.components.day.paging.firstVisibleDay
import com.infomaniak.calendar.components.day.paging.requestFirstVisibleDay
import com.infomaniak.calendar.components.day.state.DayColumnsState

/**
 * Zooms the days of a lazy row by widening its columns, keeping the day under the fingers in place
 * by moving the row's first visible day.
 *
 * The row's position being counted in days, which a zoom leaves unchanged, it never has to wait for
 * the content to be measured at its new size the way a [ZoomableAxisState] does.
 *
 * The pinch is read as an offset into the row's viewport, so [pinchToZoom] goes on the row itself,
 * outside of its scroll.
 */
internal class DayStripZoomAxis(
    private val columnsState: DayColumnsState,
    private val stripState: LazyListState,
) : ZoomableAxis {

    override var isZooming: Boolean by columnsState::isPinching

    /** The anchor's position is the day under the fingers, fractional part included. */
    override fun anchorAt(pointerOffset: Float): ZoomAnchor {
        val columnWidth = columnWidth()
        return ZoomAnchor(
            position = stripState.firstVisibleDay(columnWidth) + pointerOffset / columnWidth,
            viewportOffset = pointerOffset,
        )
    }

    override fun zoomAround(anchor: ZoomAnchor, factor: Float) {
        columnsState.zoom *= factor

        val columnWidth = columnWidth()
        stripState.requestFirstVisibleDay(anchor.position - anchor.viewportOffset / columnWidth, columnWidth)
    }

    private fun columnWidth(): Int {
        return columnsState.columnWidthPx(stripState.layoutInfo.viewportSize.width).coerceAtLeast(1)
    }
}
