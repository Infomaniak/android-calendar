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

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import com.infomaniak.scrollpicker.ScrollPickerGroupState
import com.infomaniak.scrollpicker.ScrollPickerState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

/**
 * The wheels' own state, built from a [TimeScrollPickerState] they show rather than saved, so they can't hold another time.
 * Both clocks' hour wheels share [hourState], whose index is the hour of the day.
 */
@Stable
internal class TimeWheels(time: LocalTime) {
    private val group = ScrollPickerGroupState()

    val hourState = ScrollPickerState(initialIndex = time.hour, group = group)
    val minuteState = ScrollPickerState(initialIndex = time.minute, group = group)
    val amPmState = ScrollPickerState(initialIndex = time.hour / 12, group = group)

    private val hour: Int get() = hourState.selectedIndex
    private val minute: Int get() = minuteState.selectedIndex

    /** Moves the wheels to the [state]'s time when it's set, and hands it the times picked on them. */
    suspend fun followAndPickOn(state: TimeScrollPickerState) = coroutineScope {
        launch { snapshotFlow { state.time }.collect { if (!shows(it)) select(it) } }
        launch {
            snapshotFlow { LocalTime(hour, minute) }.collect {
                if (shows(state.time)) return@collect
                state.pick(it)
                // The pick may be rejected or changed without changing the state's time, which the first flow wouldn't see.
                if (!shows(state.time)) select(state.time)
            }
        }
    }

    /** Shows the hour's half of the day on the AM/PM wheel, and moves the hour by 12 when AM/PM is picked. */
    suspend fun keepAmPmSyncedWithHourHalf() = coroutineScope {
        launch { snapshotFlow { hour / 12 }.collect { amPmState.selectedIndex = it } }
        launch {
            snapshotFlow { amPmState.selectedIndex }.collect {
                // Lands on the same label, so the jump doesn't show.
                if (it != hour / 12) hourState.select(hour % 12 + 12 * it)
            }
        }
    }

    private fun shows(time: LocalTime) = hour == time.hour && minute == time.minute

    private fun select(time: LocalTime) {
        hourState.selectedIndex = time.hour
        minuteState.selectedIndex = time.minute
    }
}
