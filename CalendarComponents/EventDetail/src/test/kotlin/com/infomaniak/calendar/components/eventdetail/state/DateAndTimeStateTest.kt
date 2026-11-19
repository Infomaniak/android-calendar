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
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class DateAndTimeStateTest {

    private fun entry(hour: Int) = DateAndTimeState.Entry(
        date = mutableStateOf(LocalDate(2026, 11, 16)),
        timeState = TimeScrollPickerState(LocalTime(hour, 0)),
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
    fun scrollingTheStartPastATouchedEnd_keepsTheDurationTheSessionBeganWith() = with(
        DateAndTimeState(start = entry(10), end = entry(11), hasEndEverBeenTouched = mutableStateOf(true)),
    ) {
        val session = TimeSession(this, start)

        (11..12).forEach { session.pickTime(LocalTime(it, 0)) }
        assertEquals(LocalTime(13, 0), end.time)
    }
}
