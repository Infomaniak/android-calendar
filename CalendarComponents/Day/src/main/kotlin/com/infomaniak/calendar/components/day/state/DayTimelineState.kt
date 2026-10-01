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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.model.HOURS_PER_DAY
import com.infomaniak.calendar.components.day.model.MINUTES_PER_HOUR
import com.infomaniak.calendar.components.day.zoom.ZoomableAxisState
import com.infomaniak.calendar.components.day.zoom.rememberZoomableAxisState
import kotlinx.coroutines.flow.first

/** The hour a day opens on: early enough to have the morning in view, late enough to skip the night. */
private const val INITIAL_VISIBLE_HOUR = 8

/** The hour height a zoom of 1 stands for. */
private val BaseHourHeight = DayTimelineDefaults.HourHeight

/**
 * The timeline opens on [INITIAL_VISIBLE_HOUR], unless [scrollState] was restored to a position the
 * user had already scrolled to.
 *
 * [initialHourHeight] is read once, when the state is built: the state owns its height from then on.
 */
@Composable
fun rememberDayTimelineState(
    initialHourHeight: Dp = DayTimelineDefaults.HourHeight,
    scrollState: ScrollState = rememberScrollState(),
): DayTimelineState {
    val zoomAxis = rememberZoomableAxisState(
        initialZoom = initialHourHeight / BaseHourHeight,
        zoomRange = DayTimelineDefaults.MinHourHeight / BaseHourHeight..DayTimelineDefaults.MaxHourHeight / BaseHourHeight,
        scrollState = scrollState,
    )
    val state = remember(zoomAxis) { DayTimelineState(zoomAxis) }
    val density = LocalDensity.current

    LaunchedEffect(state) {
        if (scrollState.value != 0) return@LaunchedEffect

        // Scrolling before the timeline has been measured would be clamped to a scroll range of zero.
        snapshotFlow { scrollState.maxValue }.first { it > 0 }
        state.scrollToMinuteOfDay(INITIAL_VISIBLE_HOUR * MINUTES_PER_HOUR, density)
    }

    return state
}

/**
 * Vertical geometry of the timeline: how tall an hour is, where the timeline is scrolled, and the
 * translation between a minute of the day and a vertical offset.
 *
 * A single instance is shared by every page of a pager, so swiping to other days keeps the same
 * zoom level and the same scroll position.
 */
@Stable
class DayTimelineState internal constructor(internal val zoomAxis: ZoomableAxisState) {

    val scrollState: ScrollState get() = zoomAxis.scrollState

    val isPinching: Boolean get() = zoomAxis.isZooming

    var hourHeight: Dp
        get() = BaseHourHeight * zoomAxis.zoom
        set(value) {
            zoomAxis.zoom = value / BaseHourHeight
        }

    val timelineHeight: Dp get() = hourHeight * HOURS_PER_DAY

    fun verticalOffsetOf(minuteOfDay: Int): Dp = hourHeight * (minuteOfDay / MINUTES_PER_HOUR.toFloat())

    suspend fun scrollToMinuteOfDay(minuteOfDay: Int, density: Density) {
        val target = verticalOffsetOf(minuteOfDay) - hourHeight

        scrollState.scrollTo(with(density) { target.roundToPx() }.coerceAtLeast(0))
    }
}
