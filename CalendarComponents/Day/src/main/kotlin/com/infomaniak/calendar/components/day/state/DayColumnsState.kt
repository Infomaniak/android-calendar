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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt

/**
 * Opens fully zoomed out, on [maxVisibleDayCount] days.
 *
 * @param maxVisibleDayCount How many days share the width when zoomed out the most.
 * @param minVisibleDayCount How many days still share the width when zoomed in the most. Set it to
 * [maxVisibleDayCount] to leave the width of the days out of a pinch.
 */
@Composable
fun rememberDayColumnsState(maxVisibleDayCount: Int, minVisibleDayCount: Int = 1): DayColumnsState {
    require(minVisibleDayCount in 1..maxVisibleDayCount)

    return remember(maxVisibleDayCount, minVisibleDayCount) { DayColumnsState(maxVisibleDayCount, minVisibleDayCount) }
}

/**
 * How many days the viewport of a multi-day timeline holds, which is how wide each of them is.
 *
 * It knows nothing of where the days are scrolled, so it holds just as well for a row that pages a
 * week at a time as for one that steps a day at a time.
 */
@Stable
class DayColumnsState internal constructor(private val maxVisibleDayCount: Int, minVisibleDayCount: Int) {

    private val zoomRange = 1f..maxVisibleDayCount / minVisibleDayCount.toFloat()

    internal val isZoomable: Boolean get() = zoomRange.endInclusive > zoomRange.start

    private var clampedZoom by mutableFloatStateOf(1f)

    /** How many times wider than when zoomed out the most a day is. */
    internal var zoom: Float
        get() = clampedZoom
        set(value) {
            clampedZoom = value.coerceIn(zoomRange)
        }

    var isPinching: Boolean by mutableStateOf(false)
        internal set

    /** How many days the viewport holds, the last one possibly in part. */
    val visibleDayCount: Float get() = maxVisibleDayCount / zoom

    /** Whole pixels, so that a number of days is always exactly that many columns wide. */
    internal fun columnWidthPx(viewportWidthPx: Int): Int = (viewportWidthPx / visibleDayCount).roundToInt()
}
