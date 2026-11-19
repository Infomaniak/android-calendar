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
class TimeScrollPickerStateTest {

    @Test
    fun onlyTimesPickedOnTheWheelsReachTheState() = runTest {
        val state = TimeScrollPickerState(LocalTime(10, 0, 30))
        val picks = mutableListOf<LocalTime>()
        backgroundScope.launch { state.syncWheels { picks += it; state.time = it } }

        settle()
        assertEquals(emptyList<LocalTime>(), picks)

        state.time = LocalTime(14, 0)
        settle()
        assertEquals(14, state.hourState.selectedIndex)
        assertEquals(emptyList<LocalTime>(), picks)

        state.minuteState.select(15)
        settle()
        assertEquals(listOf(LocalTime(14, 15)), picks)
    }

    @Test
    fun wheelsGoBackWhenAPickIsRejected() = runTest {
        val state = TimeScrollPickerState(LocalTime(10, 0))
        backgroundScope.launch { state.syncWheels {} }
        settle()

        state.minuteState.select(15)
        settle()
        assertEquals(0, state.minuteState.selectedIndex)
    }

    @Test
    fun wheelsLeftStaleWhileHiddenDoNotPick() = runTest {
        val state = TimeScrollPickerState(LocalTime(10, 0))
        val picks = mutableListOf<LocalTime>()
        state.time = LocalTime(16, 30)

        backgroundScope.launch { state.syncWheels { picks += it } }
        settle()
        assertEquals(emptyList<LocalTime>(), picks)
        assertEquals(16, state.hourState.selectedIndex)
        assertEquals(30, state.minuteState.selectedIndex)
    }

    private fun TestScope.settle() {
        Snapshot.sendApplyNotifications()
        runCurrent()
    }
}
