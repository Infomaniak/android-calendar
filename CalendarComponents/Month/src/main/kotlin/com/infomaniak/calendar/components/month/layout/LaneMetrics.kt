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

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

/**
 * The metrics stacking events in a week, derived from the sizes the day numbers and event chips are drawn with, so the
 * lanes always start under the day numbers and are as tall as a chip, whatever the font scale. Each size is rounded on
 * its own, as the cell rounds it when drawing.
 */
@Composable
internal fun laneMetrics(): LaneMetrics = with(LocalDensity.current) {
    LaneMetrics(
        headerHeight = MonthDefaults.DayNumberSize.roundToPx() + MonthDefaults.DayNumberPadding.roundToPx() * 2,
        laneHeight = MonthDefaults.LaneHeight.roundToPx(),
        laneSpacing = MonthDefaults.LaneSpacing.roundToPx(),
    )
}

/**
 * How events stack under a week's day numbers, in pixels: the lanes start below [headerHeight], each [laneHeight] tall
 * and followed by [laneSpacing].
 */
internal data class LaneMetrics(val headerHeight: Int, val laneHeight: Int, val laneSpacing: Int) {

    private val laneStep get() = laneHeight + laneSpacing

    /** How many lanes fit in a row [rowHeight] tall, counting the spacing below the last one. */
    fun laneCount(rowHeight: Int): Int = ((rowHeight - headerHeight) / laneStep).coerceAtLeast(0)

    /** The smallest row fitting [laneCount] lanes, the inverse of [laneCount]: it ends where the next lane would start. */
    fun rowHeightFor(laneCount: Int): Int = laneTop(laneCount)

    fun laneTop(lane: Int): Int = headerHeight + lane * laneStep
}
