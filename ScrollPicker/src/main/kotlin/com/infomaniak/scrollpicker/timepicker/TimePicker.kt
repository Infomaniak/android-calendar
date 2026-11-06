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

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.scrollpicker.ScrollPicker
import com.infomaniak.scrollpicker.ScrollPickerDefaults
import com.infomaniak.scrollpicker.ScrollPickerState
import com.infomaniak.scrollpicker.rememberScrollPickerGroupState
import com.infomaniak.scrollpicker.rememberScrollPickerState
import java.time.LocalTime

@Composable
fun TimePicker(state: TimePickerState, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (DateFormat.is24HourFormat(LocalContext.current)) {
            HourPicker24(state)
            Text(text = ":", style = ScrollPickerDefaults.textStyle)
            MinutePicker(state)
        } else {
            HourPicker12(state)
            Text(text = ":", style = ScrollPickerDefaults.textStyle)
            MinutePicker(state)
            AmPmPicker(state)
        }
    }
}

@Composable
private fun HourPicker24(state: TimePickerState) {
    ScrollPicker(
        items = (0..23).toList(),
        state = state.hourState,
        loop = true,
        itemLabel = ::twoDigits,
        onInput = String::toIntOrNull,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = Modifier.width(100.dp),
    )
}

@Composable
private fun MinutePicker(state: TimePickerState) {
    ScrollPicker(
        items = (0..59).toList(),
        state = state.minuteState,
        loop = true,
        itemLabel = ::twoDigits,
        onInput = String::toIntOrNull,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        modifier = Modifier.width(100.dp),
    )
}

@Composable
private fun HourPicker12(state: TimePickerState) {
    TODO()
}

@Composable
private fun AmPmPicker(state: TimePickerState) {
    TODO()
}

@Composable
fun rememberTimePickerState(initialTime: LocalTime = LocalTime.of(0, 0)): TimePickerState {
    val group = rememberScrollPickerGroupState()
    val hourState: ScrollPickerState = rememberScrollPickerState(initialIndex = initialTime.hour, group = group)
    val minuteState: ScrollPickerState = rememberScrollPickerState(initialIndex = initialTime.minute, group = group)

    return TimePickerState(hourState = hourState, minuteState = minuteState)
}

@Stable
class TimePickerState(
    internal val hourState: ScrollPickerState,
    internal val minuteState: ScrollPickerState,
) {
    val hour: Int get() = hourState.selectedIndex
    val minute: Int get() = minuteState.selectedIndex
}

private fun twoDigits(number: Int) = number.toString().padStart(2, '0')

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            TimePicker(rememberTimePickerState(LocalTime.of(10, 30)))
        }
    }
}
