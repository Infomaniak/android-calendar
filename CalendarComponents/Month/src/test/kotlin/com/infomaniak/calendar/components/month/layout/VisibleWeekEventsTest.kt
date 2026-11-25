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

import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.SimpleEventSummaryUi
import com.infomaniak.calendar.components.foundation.preview.EventColorsUiFactory
import com.infomaniak.calendar.components.month.model.MonthEventSegmentUi
import com.infomaniak.calendar.components.month.model.MonthWeekUi
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

class VisibleWeekEventsTest {

    @Test
    fun anEmptyWeekShowsNothingAndHidesNothing() {
        val visibleEvents = week().visibleEventsIn(laneCount = 3)

        assertEquals(emptyList<MonthEventSegmentUi>(), visibleEvents.segments)
        assertEquals(List(7) { false }, visibleEvents.hasHiddenEvents)
    }

    @Test
    fun theLastLaneShowsAnEventOnlyWhenNoneOfItsDaysOverflows() {
        val fitting = segment(startColumn = 0, span = 2, lane = 1)
        val aboveTheOverflow = segment(startColumn = 4, span = 1, lane = 0)
        val overflowing = segment(startColumn = 3, span = 2, lane = 1)
        val beyond = segment(startColumn = 4, span = 1, lane = 2)

        val visibleEvents = week(fitting, aboveTheOverflow, overflowing, beyond).visibleEventsIn(laneCount = 2)

        assertEquals(listOf(fitting, aboveTheOverflow), visibleEvents.segments)
        assertEquals(listOf(false, false, false, true, true, false, false), visibleEvents.hasHiddenEvents)
    }

    @Test
    fun aHiddenSpanningEventIsHiddenInEveryDayItCovers() {
        val first = segment(startColumn = 0, span = 1, lane = 0)
        val spanning = segment(startColumn = 0, span = 4, lane = 1)

        val visibleEvents = week(first, spanning).visibleEventsIn(laneCount = 1)

        assertEquals(emptyList<MonthEventSegmentUi>(), visibleEvents.segments)
        assertEquals(listOf(true, true, true, true, false, false, false), visibleEvents.hasHiddenEvents)
    }

    @Test
    fun noLaneHidesEveryEvent() {
        val event = segment(startColumn = 2, span = 2, lane = 0)

        val visibleEvents = week(event).visibleEventsIn(laneCount = 0)

        assertEquals(emptyList<MonthEventSegmentUi>(), visibleEvents.segments)
        assertEquals(listOf(false, false, true, true, false, false, false), visibleEvents.hasHiddenEvents)
    }

    private fun week(vararg segments: MonthEventSegmentUi): MonthWeekUi {
        val monday = LocalDate(2026, 6, 1)
        return MonthWeekUi(dates = List(7) { monday.plus(DatePeriod(days = it)) }, segments = segments.toList())
    }

    private fun segment(startColumn: Int, span: Int, lane: Int) = MonthEventSegmentUi(
        event = SimpleEventSummaryUi(
            id = "$startColumn-$span-$lane",
            title = "Event",
            location = null,
            status = EventStatus.Confirmed,
            start = Instant.fromEpochSeconds(0),
            end = Instant.fromEpochSeconds(0),
            isAllDay = true,
            colors = EventColorsUiFactory.dummyEventColorsUiFactory.create(0),
            hasAttendees = false,
            myStatus = null,
        ),
        startColumn = startColumn,
        span = span,
        lane = lane,
    )
}
