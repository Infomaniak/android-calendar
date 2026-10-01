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
package com.infomaniak.calendar.components.day.state

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.infomaniak.calendar.components.day.zoom.ZoomableAxisState
import com.infomaniak.calendar.components.day.zoom.rememberZoomableAxisState

/**
 * Opens fully zoomed out, on [maxVisibleDayCount] days.
 *
 * @param maxVisibleDayCount How many days share the width when zoomed out the most, usually the
 * number of days of a page.
 * @param minVisibleDayCount How many days still share the width when zoomed in the most.
 */
@Composable
fun rememberDayColumnsState(
    maxVisibleDayCount: Int,
    minVisibleDayCount: Int = 1,
    scrollState: ScrollState = rememberScrollState(),
): DayColumnsState {
    require(minVisibleDayCount in 1..maxVisibleDayCount)

    val zoomAxis = rememberZoomableAxisState(
        initialZoom = 1f,
        zoomRange = 1f..maxVisibleDayCount / minVisibleDayCount.toFloat(),
        scrollState = scrollState,
    )

    return remember(zoomAxis, maxVisibleDayCount) { DayColumnsState(zoomAxis, maxVisibleDayCount) }
}

/**
 * Horizontal geometry of a multi-day timeline: how wide a day is, and where the days are scrolled
 * once zoomed in past the point where they all fit.
 *
 * Like [DayTimelineState], a single instance is meant to be shared by every page of a pager.
 */
@Stable
class DayColumnsState internal constructor(
    internal val zoomAxis: ZoomableAxisState,
    private val maxVisibleDayCount: Int,
) {

    val scrollState: ScrollState get() = zoomAxis.scrollState

    val isPinching: Boolean get() = zoomAxis.isZooming

    /** How many days the viewport holds, the last one possibly in part. */
    val visibleDayCount: Float get() = maxVisibleDayCount / zoomAxis.zoom

    fun columnWidth(viewportWidth: Dp): Dp = viewportWidth / visibleDayCount
}
