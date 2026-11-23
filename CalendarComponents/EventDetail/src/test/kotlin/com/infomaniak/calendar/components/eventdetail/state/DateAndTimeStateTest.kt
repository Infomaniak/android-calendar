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
package com.infomaniak.calendar.components.eventdetail.state

import androidx.compose.runtime.mutableStateOf
import com.infomaniak.scrollpicker.timepicker.TimeScrollPickerState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class DateAndTimeStateTest {

    private fun entry(hour: Int, minute: Int = 0, day: Int = 16) = DateAndTimeState.Entry(
        date = mutableStateOf(LocalDate(2026, 11, day)),
        timeState = TimeScrollPickerState(LocalTime(hour, minute)),
        timeZone = mutableStateOf(TimeZone.UTC),
    )

    @Test
    fun scrollingTheEndBeforeTheStart_keepsTheDuration_andLeavesTheStartWhenScrolledBack() = with(
        DateAndTimeState(start = entry(10), end = entry(11), hasEndEverBeenTouched = mutableStateOf(false)),
    ) {
        val session = TimeSession(this, end)

        (10 downTo 8).forEach { session.pickTime(LocalTime(it, 0)) }
        assertEquals(LocalTime(7, 0), start.time)

        (9..12).forEach { session.pickTime(LocalTime(it, 0)) }
        assertEquals(LocalTime(7, 0), start.time)
    }

    @Test
    fun aNewSession_keepsTheDurationSetInThePreviousOne() = with(
        DateAndTimeState(start = entry(10), end = entry(11), hasEndEverBeenTouched = mutableStateOf(false)),
    ) {
        TimeSession(this, end).pickTime(LocalTime(12, 0))

        val session = TimeSession(this, end)
        (11 downTo 9).forEach { session.pickTime(LocalTime(it, 0)) }
        assertEquals(LocalTime(7, 0), start.time)
    }

    @Test
    fun scrollingTheStartPastATouchedEnd_keepsTheDurationTheSessionBeganWith_andLeavesTheEndWhenScrolledBack() = with(
        DateAndTimeState(start = entry(10), end = entry(11), hasEndEverBeenTouched = mutableStateOf(true)),
    ) {
        val session = TimeSession(this, start)

        (11..13).forEach { session.pickTime(LocalTime(it, 0)) }
        assertEquals(LocalTime(14, 0), end.time)

        (12 downTo 10).forEach { session.pickTime(LocalTime(it, 0)) }
        assertEquals(LocalTime(14, 0), end.time)
    }

    @Test
    fun scrollingTheEndPastMidnightBeforeTheStart_keepsTheDuration() = with(
        DateAndTimeState(
            start = entry(0, minute = 30),
            end = entry(1, minute = 30),
            hasEndEverBeenTouched = mutableStateOf(false),
        ),
    ) {
        val session = TimeSession(this, end)

        listOf(LocalTime(0, 30), LocalTime(0, 29), LocalTime(0, 28)).forEach(session::pickTime)
        assertEquals(LocalDate(2026, 11, 15), start.date)
        assertEquals(LocalTime(23, 28), start.time)
    }

    @Test
    fun aDatePickedDuringASession_restartsIt() = with(
        DateAndTimeState(start = entry(10), end = entry(11), hasEndEverBeenTouched = mutableStateOf(false)),
    ) {
        val session = TimeSession(this, end)

        start.pickDate(LocalDate(2026, 11, 18))
        session.pickTime(LocalTime(9, 0))
        assertEquals(LocalDateTime(2026, 11, 18, 8, 0), start.date.atTime(start.time))
    }

    @Test
    fun pickingTheStartDateAfterTheEnd_movesTheEndKeepingTheDuration() = with(
        DateAndTimeState(start = entry(10), end = entry(11), hasEndEverBeenTouched = mutableStateOf(true)),
    ) {
        start.pickDate(LocalDate(2026, 11, 18))
        assertEquals(LocalDate(2026, 11, 18), end.date)
        assertEquals(LocalTime(11, 0), end.time)
    }

    @Test
    fun pickingTheEndDateBeforeTheStart_movesTheStartKeepingTheDuration() = with(
        DateAndTimeState(start = entry(10), end = entry(11, day = 17), hasEndEverBeenTouched = mutableStateOf(false)),
    ) {
        end.pickDate(LocalDate(2026, 11, 14))
        assertEquals(LocalDate(2026, 11, 13), start.date)
        assertEquals(LocalTime(10, 0), start.time)
    }

    @Test
    fun changingASharedTimeZone_leavesATouchedEndThatStaysAfterTheStart() = with(
        DateAndTimeState(
            start = entry(0, minute = 30),
            end = entry(3, minute = 30),
            hasEndEverBeenTouched = mutableStateOf(true),
        ),
    ) {
        // The night New York moves to summer time, so 00:30 and 03:30 there are only two hours apart
        start.date = LocalDate(2026, 3, 8)
        end.date = LocalDate(2026, 3, 8)
        start.pickTimeZone(TimeZone.of("America/New_York"))
        assertEquals(LocalTime(3, 30), end.time)
    }

    @Test
    fun typingAnEndTime_dropsTheCorrectionsOfItsPrefix() = with(
        DateAndTimeState(start = entry(10), end = entry(11), hasEndEverBeenTouched = mutableStateOf(true)),
    ) {
        val session = TimeSession(this, end, isTyping = true)

        session.pickTime(LocalTime(1, 0))
        session.pickTime(LocalTime(12, 0))
        assertEquals(LocalTime(10, 0), start.time)
        assertEquals(LocalTime(12, 0), end.time)
    }

    @Test
    fun typingAnEndTime_keepsADatePickedDuringTheInput() = with(
        DateAndTimeState(start = entry(10), end = entry(11), hasEndEverBeenTouched = mutableStateOf(true)),
    ) {
        val session = TimeSession(this, end, isTyping = true)

        session.pickTime(LocalTime(12, 0))
        end.pickDate(LocalDate(2026, 11, 17))
        session.pickTime(LocalTime(13, 0))
        assertEquals(LocalDate(2026, 11, 17), end.date)
    }
}
