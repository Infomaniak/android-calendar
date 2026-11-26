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
package com.infomaniak.calendar.ui.screen.month.pendingKmp

import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.ThemedColorUi
import com.infomaniak.calendar.components.month.model.MonthEventSegmentUi
import com.infomaniak.calendar.ui.model.OccurrenceEventSummaryUi
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

class WeekPackingTest {

    @Test
    fun `weeks run from the week of the month's first day to the week of its last day`() {
        assertEquals(4, buildMonth(YearMonth(2027, 2), DayOfWeek.MONDAY, emptyMap()).weeks.size)
        assertEquals(5, buildMonth(MARCH, DayOfWeek.MONDAY, emptyMap()).weeks.size)
        assertEquals(6, buildMonth(YearMonth(2027, 8), DayOfWeek.MONDAY, emptyMap()).weeks.size)
    }

    @Test
    fun `weeks are 7 consecutive dates starting on the first day of week`() {
        val weeks = buildMonth(YearMonth(2027, 2), DayOfWeek.SUNDAY, emptyMap()).weeks

        assertEquals(5, weeks.size)
        weeks.forEachIndexed { index, week ->
            val weekStart = LocalDate(2027, 1, 31).plus(index, DateTimeUnit.WEEK)
            assertEquals(List(7) { weekStart.plus(it, DateTimeUnit.DAY) }, week.dates)
        }
    }

    @Test
    fun `single-day events stack in display order`() {
        val placed = pack("a" to march(1), "b" to march(1))

        assertEquals(listOf(Placed("a", 0..0, lane = 0), Placed("b", 0..0, lane = 1)), placed)
    }

    @Test
    fun `consecutive days of an occurrence make one segment`() {
        assertEquals(listOf(Placed("a", 0..2, lane = 0)), pack("a" to march(1, 2, 3)))
    }

    @Test
    fun `a day without the occurrence splits it into two segments`() {
        val placed = pack("a" to march(1, 2, 4))

        assertEquals(listOf(Placed("a", 0..1, lane = 0), Placed("a", 3..3, lane = 0)), placed)
    }

    @Test
    fun `dates outside the month show no events`() {
        val lastWeek = buildMonth(MARCH, DayOfWeek.MONDAY, eventsByDate("a" to march(30, 31) + april(1, 2))).weeks.last()

        assertEquals(listOf(Placed("a", 1..2, lane = 0)), lastWeek.segments.map(::Placed))
    }

    @Test
    fun `multi-day segments take the top lanes`() {
        val placed = pack("b" to march(1), "a" to march(1, 2))

        assertEquals(listOf(Placed("a", 0..1, lane = 0), Placed("b", 0..0, lane = 1)), placed)
    }

    @Test
    fun `multi-day segments are placed earliest first, then longest first`() {
        val placed = pack("a" to march(2, 3), "c" to march(1, 2), "d" to march(2, 3, 4, 5))

        assertEquals(
            listOf(Placed("c", 0..1, lane = 0), Placed("d", 1..4, lane = 1), Placed("a", 1..2, lane = 2)),
            placed,
        )
    }

    @Test
    fun `a segment takes the lowest lane free on all its columns`() {
        val placed = pack("c" to march(1), "a" to march(1, 2), "b" to march(3))

        assertEquals(
            listOf(Placed("a", 0..1, lane = 0), Placed("c", 0..0, lane = 1), Placed("b", 2..2, lane = 0)),
            placed,
        )
    }

    /** Packs the first week of [MARCH], Monday 1st to Sunday 7th. */
    private fun pack(vararg occurrences: Pair<String, List<LocalDate>>): List<Placed> {
        return packWeek(march(*(1..7).toList().toIntArray()), MARCH, eventsByDate(*occurrences)).segments.map(::Placed)
    }

    /** Each day's events in the order of [occurrences], one slice per date of each. */
    private fun eventsByDate(vararg occurrences: Pair<String, List<LocalDate>>): Map<LocalDate, List<OccurrenceEventSummaryUi>> {
        return occurrences
            .flatMap { (occurrence, dates) -> dates.map { date -> date to slice(occurrence, date) } }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
    }

    private fun slice(occurrence: String, date: LocalDate) = OccurrenceEventSummaryUi(
        id = "$occurrence@$date",
        occurrenceId = OccurrenceId.parse(occurrence),
        title = occurrence,
        location = null,
        status = EventStatus.Confirmed,
        start = Instant.DISTANT_PAST,
        end = Instant.DISTANT_FUTURE,
        isAllDay = true,
        colors = EventColorsUi(0, 0, 0, ThemedColorUi(0, 0), 0, ThemedColorUi(0, 0)),
        hasAttendees = false,
        myStatus = null,
        eventStart = date.atTime(0, 0),
        eventEnd = date.atTime(0, 0),
    )

    private fun march(vararg days: Int) = days.map { LocalDate(2027, 3, it) }
    private fun april(vararg days: Int) = days.map { LocalDate(2027, 4, it) }

    private data class Placed(val occurrence: String, val columns: IntRange, val lane: Int) {
        constructor(segment: MonthEventSegmentUi) : this(segment.event.id.substringBefore('@'), segment.columns, segment.lane)
    }

    private companion object {
        /** Starts on a Monday. */
        val MARCH = YearMonth(2027, 3)
    }
}
