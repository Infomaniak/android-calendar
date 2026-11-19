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
package com.infomaniak.scrollpicker.timepicker

import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimeWheelsTest {

    @Test
    fun onlyTimesPickedOnTheWheelsReachTheState() = runTest {
        val state = TimeScrollPickerState(LocalTime(10, 0, 30))
        val picks = mutableListOf<LocalTime>()
        state.onTimeChange = { picks += it; state.time = it }
        val wheels = TimeWheels(state.time)
        backgroundScope.launch { wheels.followAndPickOn(state) }

        settle()
        assertEquals(emptyList<LocalTime>(), picks)

        state.time = LocalTime(14, 0)
        settle()
        assertEquals(14, wheels.hourState.selectedIndex)
        assertEquals(emptyList<LocalTime>(), picks)

        wheels.minuteState.select(15)
        settle()
        assertEquals(listOf(LocalTime(14, 15)), picks)
    }

    @Test
    fun wheelsGoBackWhenAPickIsRejected() = runTest {
        val state = TimeScrollPickerState(LocalTime(10, 0))
        state.onTimeChange = {}
        val wheels = TimeWheels(state.time)
        backgroundScope.launch { wheels.followAndPickOn(state) }
        settle()

        wheels.minuteState.select(15)
        settle()
        assertEquals(0, wheels.minuteState.selectedIndex)
    }

    private fun TestScope.settle() {
        Snapshot.sendApplyNotifications()
        runCurrent()
    }
}
