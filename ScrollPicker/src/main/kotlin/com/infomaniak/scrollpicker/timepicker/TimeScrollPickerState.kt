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
import kotlinx.datetime.LocalTime

@Composable
fun rememberTimeScrollPickerState(initialTime: LocalTime = LocalTime(0, 0)): TimeScrollPickerState {
    return rememberSaveable(saver = TimeScrollPickerState.Saver) { TimeScrollPickerState(initialTime) }
}

/**
 * The single source of truth of a [TimeScrollPicker], as `TextFieldState` is for a text field: the wheels only show [time]
 * and hand it the times picked on them. Holds no UI state, so it can live outside composition.
 */
@Stable
class TimeScrollPickerState(initialTime: LocalTime = LocalTime(0, 0)) {

    /** Setting it moves the wheels there. Its seconds are kept, the wheels only showing hours and minutes. */
    var time by mutableStateOf(initialTime)

    /** Called with each time picked on the wheels instead of setting [time], like `SliderState.onValueChange`. */
    var onTimeChange: ((LocalTime) -> Unit)? = null

    internal fun pick(time: LocalTime) {
        val onTimeChange = onTimeChange
        if (onTimeChange == null) this.time = time else onTimeChange(time)
    }

    companion object {
        val Saver = Saver<TimeScrollPickerState, LocalTime>(save = { it.time }, restore = ::TimeScrollPickerState)
    }
}
