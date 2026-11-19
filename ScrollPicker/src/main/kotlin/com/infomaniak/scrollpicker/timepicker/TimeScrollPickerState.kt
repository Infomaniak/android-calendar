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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.infomaniak.scrollpicker.ScrollPickerGroupState
import com.infomaniak.scrollpicker.ScrollPickerState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

@Composable
fun rememberTimeScrollPickerState(initialTime: LocalTime = LocalTime(0, 0)): TimeScrollPickerState {
    return rememberSaveable(saver = TimeScrollPickerState.Saver) { TimeScrollPickerState(initialTime) }
}

/**
 * The single source of truth of a [TimeScrollPicker], as `TextFieldState` is for a text field: the wheels only show [time]
 * and hand it the times picked on them. Only [time] is saved, the wheels being rebuilt from it, so they can't restore another.
 */
@Stable
class TimeScrollPickerState(initialTime: LocalTime = LocalTime(0, 0)) {

    /** Setting it moves the wheels there. Its seconds are kept, the wheels only showing hours and minutes. */
    var time by mutableStateOf(initialTime)

    /** Called with each time picked on the wheels instead of setting [time], like `SliderState.onValueChange`. */
    var onTimeChange: ((LocalTime) -> Unit)? = null

    private val group = ScrollPickerGroupState()

    /** Shared by both clocks' hour wheels, its index is the hour of the day. */
    internal val hourState = ScrollPickerState(initialIndex = initialTime.hour, group = group)
    internal val minuteState = ScrollPickerState(initialIndex = initialTime.minute, group = group)
    internal val amPmState = ScrollPickerState(initialIndex = initialTime.hour / 12, group = group)

    private val hour: Int get() = hourState.selectedIndex
    private val minute: Int get() = minuteState.selectedIndex

    /** Moves the wheels to [time] when it's set, and hands it the times picked on them, while the picker is shown. */
    internal suspend fun syncWheels() = coroutineScope {
        // The wheels kept their position while the picker was hidden, so they must show [time] before being observed.
        if (!shows(time)) jumpTo(time)

        launch { snapshotFlow { time }.collect { if (!shows(it)) select(it) } }
        launch {
            snapshotFlow { LocalTime(hour, minute) }.collect {
                if (shows(time)) return@collect
                pick(it)
                // The pick may be rejected or changed without changing [time], which the first flow wouldn't see.
                if (!shows(time)) select(time)
            }
        }
    }

    /** Shows the hour's half of the day on the AM/PM wheel, and moves the hour by 12 when AM/PM is picked. */
    internal suspend fun keepAmPmSyncedWithHourHalf() = coroutineScope {
        launch { snapshotFlow { hour / 12 }.collect { amPmState.selectedIndex = it } }
        launch {
            snapshotFlow { amPmState.selectedIndex }.collect {
                // Lands on the same label, so the jump doesn't show.
                if (it != hour / 12) hourState.select(hour % 12 + 12 * it)
            }
        }
    }

    private fun pick(time: LocalTime) {
        val onTimeChange = onTimeChange
        if (onTimeChange == null) this.time = time else onTimeChange(time)
    }

    private fun shows(time: LocalTime) = hour == time.hour && minute == time.minute

    private fun select(time: LocalTime) {
        hourState.selectedIndex = time.hour
        minuteState.selectedIndex = time.minute
    }

    private fun jumpTo(time: LocalTime) {
        hourState.select(time.hour)
        minuteState.select(time.minute)
        amPmState.select(time.hour / 12)
    }

    companion object {
        val Saver = Saver<TimeScrollPickerState, LocalTime>(save = { it.time }, restore = ::TimeScrollPickerState)
    }
}
