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
package com.infomaniak.calendar.components.eventdetail.form

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class EventDatesTest {

    private val paris = TimeZone.of("Europe/Paris")
    private val tokyo = TimeZone.of("Asia/Tokyo")
    private val london = TimeZone.of("Europe/London")
    private val day = LocalDate(2026, 11, 16)

    private fun dates(start: LocalTime, end: LocalTime, startTimeZone: TimeZone = paris, endTimeZone: TimeZone = startTimeZone) =
        EventDates(EventDateTime(day, start, startTimeZone), EventDateTime(day, end, endTimeZone))

    @Test
    fun pickTime_onStart_movesAnUntouchedEndAlong() {
        val picked = dates(LocalTime(10, 0), LocalTime(11, 0)).pickTime(isStart = true, LocalTime(14, 0))

        assertEquals(LocalTime(14, 0), picked.start.time)
        assertEquals(LocalTime(15, 0), picked.end.time)
    }

    @Test
    fun pickTime_onStart_keepsATouchedEndWhenStillAfterIt() {
        val touched = dates(LocalTime(10, 0), LocalTime(11, 0)).pickTime(isStart = false, LocalTime(12, 0))

        val picked = touched.pickTime(isStart = true, LocalTime(9, 0))

        assertEquals(LocalTime(9, 0), picked.start.time)
        assertEquals(LocalTime(12, 0), picked.end.time)
    }

    @Test
    fun pickTime_onStart_pushesATouchedEndItWouldPass() {
        val touched = dates(LocalTime(10, 0), LocalTime(11, 0)).pickTime(isStart = false, LocalTime(12, 0))

        val picked = touched.pickTime(isStart = true, LocalTime(13, 0))

        assertEquals(LocalTime(15, 0), picked.end.time)
    }

    @Test
    fun pickTime_onEnd_beforeStart_pullsTheStartBack() {
        val picked = dates(LocalTime(10, 0), LocalTime(11, 0)).pickTime(isStart = false, LocalTime(9, 0))

        assertEquals(LocalTime(8, 0), picked.start.time)
        assertEquals(LocalTime(9, 0), picked.end.time)
        assertTrue(picked.hasEndEverBeenTouched)
    }

    @Test
    fun pickDate_onStart_movesAnUntouchedEndToTheSameDay() {
        val nextDay = LocalDate(2026, 11, 17)

        val picked = dates(LocalTime(10, 0), LocalTime(11, 0)).pickDate(isStart = true, nextDay)

        assertEquals(nextDay, picked.start.date)
        assertEquals(nextDay, picked.end.date)
        assertEquals(LocalTime(11, 0), picked.end.time)
    }

    @Test
    fun pickTimeZone_onStart_movesASyncedEndToTheSameZone() {
        val picked = dates(LocalTime(10, 0), LocalTime(11, 0)).pickTimeZone(isStart = true, tokyo)

        assertEquals(EventDateTime(day, LocalTime(10, 0), tokyo), picked.start)
        assertEquals(EventDateTime(day, LocalTime(11, 0), tokyo), picked.end)
        assertFalse(picked.hasEndEverBeenTouched)
    }

    @Test
    fun pickTimeZone_onStart_keepsTheZoneOfAnEndInAnotherOne() {
        // 19:00 in Tokyo is 11:00 in Paris, one hour after the start
        val picked = dates(LocalTime(10, 0), LocalTime(19, 0), endTimeZone = tokyo).pickTimeZone(isStart = true, london)

        assertEquals(EventDateTime(day, LocalTime(10, 0), london), picked.start)
        assertEquals(EventDateTime(day, LocalTime(20, 0), tokyo), picked.end)
    }

    @Test
    fun pickTimeZone_onEnd_onlyChangesTheEnd() {
        val picked = dates(LocalTime(10, 0), LocalTime(11, 0)).pickTimeZone(isStart = false, london)

        assertEquals(EventDateTime(day, LocalTime(10, 0), paris), picked.start)
        assertEquals(EventDateTime(day, LocalTime(11, 0), london), picked.end)
        assertTrue(picked.hasEndEverBeenTouched)
    }

    @Test
    fun default_onAnotherDay_startsAtEightForTheGivenDuration() {
        val default = EventDates.default(LocalDate(2030, 1, 15), paris, 90.minutes)

        assertEquals(EventDateTime(LocalDate(2030, 1, 15), LocalTime(8, 0), paris), default.start)
        assertEquals(EventDateTime(LocalDate(2030, 1, 15), LocalTime(9, 30), paris), default.end)
    }
}
