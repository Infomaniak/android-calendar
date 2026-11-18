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
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class MonthRowHeightsTest {

    private val metrics = LaneMetrics(headerHeight = 40, laneHeight = 20, laneSpacing = 2)
    private val fiveWeeks = List(5) { MonthWeekUi(dates = List(7) { LocalDate(2026, 5, 1) }, segments = emptyList()) }

    @Test
    fun rowsSharingTheHeightFillItExactly() {
        val heights = monthRowHeights(IntSize(700, 1003), bottomPadding = 0, fiveWeeks, metrics)

        assertEquals(200, heights.height)
        assertEquals(203, heights.lastHeight)
    }

    @Test
    fun theLastRowReachesUnderTheBottomPadding() {
        val heights = monthRowHeights(IntSize(700, 1080), bottomPadding = 80, fiveWeeks, metrics)

        assertEquals(200, heights.height)
        assertEquals(280, heights.lastHeight)
    }

    @Test
    fun squareRowsLeaveTheHeightTheyDoNotNeedEmpty() {
        val heights = monthRowHeights(IntSize(761, 585), bottomPadding = 0, fiveWeeks, metrics)

        assertEquals(108, heights.height)
        assertEquals(108, heights.lastHeight)
    }

    @Test
    fun rowsAreNeverShorterThanASingleLaneNeeds() {
        val heights = monthRowHeights(IntSize(100, 200), bottomPadding = 0, fiveWeeks, metrics)

        assertEquals(metrics.rowHeightFor(laneCount = 1), heights.height)
    }
}
