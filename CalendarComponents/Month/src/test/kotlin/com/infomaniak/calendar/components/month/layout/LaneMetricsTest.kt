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

import org.junit.Assert.assertEquals
import org.junit.Test

class LaneMetricsTest {

    private val metrics = LaneMetrics(headerHeight = 40, laneHeight = 20, laneSpacing = 2)

    @Test
    fun noLaneFitsInARowNoTallerThanItsHeader() {
        assertEquals(0, metrics.laneCount(rowHeight = 0))
        assertEquals(0, metrics.laneCount(rowHeight = 40))
        assertEquals(0, metrics.laneCount(rowHeight = 42))
    }

    @Test
    fun aLaneFitsOnlyWithItsSpacing() {
        assertEquals(0, metrics.laneCount(rowHeight = 61))
        assertEquals(1, metrics.laneCount(rowHeight = 62))
    }

    @Test
    fun rowHeightForIsTheInverseOfLaneCount() {
        val allMetrics = listOf(metrics, LaneMetrics(headerHeight = 33, laneHeight = 17, laneSpacing = 1))

        allMetrics.forEach { metrics ->
            (1..6).forEach { lanes ->
                assertEquals(lanes, metrics.laneCount(metrics.rowHeightFor(lanes)))
                assertEquals(lanes - 1, metrics.laneCount(metrics.rowHeightFor(lanes) - 1))
            }
        }
    }
}
