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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
import java.text.DateFormatSymbols
import java.time.LocalTime

private val PickerWidth = 100.dp

@Composable
fun TimePicker(state: TimePickerState, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (state.is24Hour) {
            HourPicker24(state)
            Text(text = ":", style = ScrollPickerDefaults.textStyle)
            MinutePicker(state, ImeAction.Done)
        } else {
            HourPicker12(state)
            Text(text = ":", style = ScrollPickerDefaults.textStyle)
            MinutePicker(state, ImeAction.Next)
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
        modifier = Modifier.width(PickerWidth),
    )
}

@Composable
private fun MinutePicker(state: TimePickerState, imeAction: ImeAction) {
    ScrollPicker(
        items = (0..59).toList(),
        state = state.minuteState,
        loop = true,
        itemLabel = ::twoDigits,
        onInput = String::toIntOrNull,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        modifier = Modifier.width(PickerWidth),
    )
}

/** Shows 12 first so that an item's index is the hour modulo 12. */
@Composable
private fun HourPicker12(state: TimePickerState) {
    ScrollPicker(
        items = Hours12,
        state = state.hour12State,
        loop = true,
        itemLabel = ::twoDigits,
        onInput = String::toIntOrNull,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = Modifier.width(PickerWidth),
    )
}

/** Items are 0 for AM and 1 for PM, labelled in the current locale. */
@Composable
private fun AmPmPicker(state: TimePickerState) {
    val locale = LocalConfiguration.current.locales[0]
    val labels = remember(locale) { DateFormatSymbols.getInstance(locale).amPmStrings.toList() }
    ScrollPicker(
        items = listOf(0, 1),
        state = state.amPmState,
        itemLabel = labels::get,
        onInput = { text -> labels.indexOfFirst { text.isNotEmpty() && it.startsWith(text, ignoreCase = true) }.takeIf { it >= 0 } },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        modifier = Modifier.width(PickerWidth),
    )
}

@Composable
fun rememberTimePickerState(
    initialTime: LocalTime = LocalTime.of(0, 0),
    is24Hour: Boolean = DateFormat.is24HourFormat(LocalContext.current),
): TimePickerState {
    val group = rememberScrollPickerGroupState()
    val hourState = rememberScrollPickerState(initialIndex = initialTime.hour, group = group)
    val hour12State = rememberScrollPickerState(initialIndex = initialTime.hour % 12, group = group)
    val minuteState = rememberScrollPickerState(initialIndex = initialTime.minute, group = group)
    val amPmState = rememberScrollPickerState(initialIndex = initialTime.hour / 12, group = group)

    val state = remember(is24Hour, hourState, hour12State, minuteState, amPmState) {
        TimePickerState(is24Hour, hourState, hour12State, minuteState, amPmState)
    }

    LaunchedEffect(state) { snapshotFlow { state.hour }.collect(state::syncHiddenHourPickers) }

    return state
}

@Stable
class TimePickerState(
    internal val is24Hour: Boolean,
    internal val hourState: ScrollPickerState,
    internal val hour12State: ScrollPickerState,
    internal val minuteState: ScrollPickerState,
    internal val amPmState: ScrollPickerState,
) {
    /** The hour of the day, from 0 to 23, whichever clock the picker shows. */
    val hour: Int
        get() = if (is24Hour) {
            hourState.selectedIndex
        } else {
            hour12State.selectedIndex + 12 * amPmState.selectedIndex
        }

    val minute: Int get() = minuteState.selectedIndex

    /** Mirrors [hour] into the pickers of the clock not shown, so switching clocks keeps the selected hour. */
    internal fun syncHiddenHourPickers(hour: Int) {
        if (is24Hour) {
            hour12State.select(hour % 12)
            amPmState.select(hour / 12)
        } else {
            hourState.select(hour)
        }
    }
}

private val Hours12 = listOf(12) + (1..11)

private fun twoDigits(number: Int) = number.toString().padStart(2, '0')

@Preview
@Composable
private fun Preview24() {
    MaterialTheme {
        Scaffold {
            TimePicker(
                rememberTimePickerState(LocalTime.of(10, 30), is24Hour = true),
                modifier = Modifier
                    .padding(it)
                    .padding(top = 56.dp),
            )
        }
    }
}

@Preview
@Composable
private fun Preview12() {
    MaterialTheme {
        Scaffold {
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(top = 56.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val state = rememberTimePickerState(LocalTime.of(10, 30), is24Hour = false)

                Text(LocalTime.of(state.hour, state.minute).toString(), style = MaterialTheme.typography.titleLarge)

                TimePicker(state)
            }
        }
    }
}
