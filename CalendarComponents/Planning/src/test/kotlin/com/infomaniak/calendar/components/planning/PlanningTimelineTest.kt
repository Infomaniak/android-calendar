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
package com.infomaniak.calendar.components.planning

import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanningTimelineTest {

    private val timeline = PlanningTimeline(WeekNumbering.ISO_8601)

    @Test
    fun dateAt_reversesIndexOf() {
        var date = LocalDate(2025, 12, 20)
        repeat(30) {
            assertEquals(date, timeline.dateAt(timeline.indexOf(date)))
            date = date.plus(1, DateTimeUnit.DAY)
        }
    }

    @Test
    fun indexOf_placesTheWeekHeaderBetweenSundayAndMonday() {
        val monday = LocalDate(2026, 1, 5)
        val headerIndex = timeline.indexOf(monday) - 1

        assertTrue(timeline.isWeekHeader(headerIndex))
        assertEquals(monday, timeline.dateAt(headerIndex))
        assertEquals(headerIndex - 1, timeline.indexOf(LocalDate(2026, 1, 4)))
    }

    @Test
    fun weekAt_usesTheIsoWeekOfTheDate() {
        // 2026-01-01 is a Thursday, so it belongs to week 1 of 2026, which starts on Monday 2025-12-29
        val week = timeline.weekAt(timeline.indexOf(LocalDate(2026, 1, 1)))

        assertEquals(LocalDate(2025, 12, 29), week.firstDay)
        assertEquals(1, week.weekNumber)
    }

    @Test
    fun indexOf_clampsDatesOutsideTheTimeline() {
        assertEquals(1, timeline.indexOf(LocalDate(1800, 1, 1)))
        assertTrue(timeline.indexOf(LocalDate(2300, 1, 1)) < timeline.itemCount)
    }
}
