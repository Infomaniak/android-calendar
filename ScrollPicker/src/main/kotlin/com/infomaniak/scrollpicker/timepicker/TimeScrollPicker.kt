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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import java.text.DateFormatSymbols

private val PickerWidth = 110.dp
private val Hours24 = (0..23).toList()

@Composable
fun TimeScrollPicker(state: TimeScrollPickerState, modifier: Modifier = Modifier) {
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
private fun HourPicker24(state: TimeScrollPickerState) {
    ScrollPicker(
        items = Hours24,
        state = state.hourState,
        loop = true,
        itemLabel = ::twoDigits,
        onInput = String::toIntOrNull,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = Modifier.width(PickerWidth),
    )
}

@Composable
private fun MinutePicker(state: TimeScrollPickerState, imeAction: ImeAction) {
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

/**
 * Holds the 24 hours of the day, labelled from 12 to 11 twice, so scrolling past 11 moves into the other half. The am/pm is
 * derived from which half of the loop we're on. This lets us keep a single [ScrollPickerState] for both formats.
 */
@Composable
private fun HourPicker12(state: TimeScrollPickerState) {
    LaunchedEffect(state) { state.keepAmPmSyncedWithHourHalfLoop() }

    ScrollPicker(
        items = Hours24,
        state = state.hourState,
        loop = true,
        itemLabel = ::convert24HoursItemsTo12HourLabels,
        // Keeps the half of the day already chosen, so typing "12" doesn't flip it.
        onInput = { text -> text.toIntOrNull()?.takeIf { it in 1..12 }?.let { it % 12 + 12 * (state.hour / 12) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = Modifier.width(PickerWidth),
    )
}

/** Items are 0 for AM and 1 for PM, labelled in the current locale. */
@Composable
private fun AmPmPicker(state: TimeScrollPickerState) {
    val locale = LocalConfiguration.current.locales[0]
    val labels = remember(locale) { DateFormatSymbols.getInstance(locale).amPmStrings.toList() }
    ScrollPicker(
        items = listOf(0, 1),
        state = state.amPmState,
        itemLabel = labels::get,
        onInput = { text ->
            labels.indexOfFirst { text.isNotEmpty() && it.startsWith(text, ignoreCase = true) }.takeIf { it >= 0 }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        modifier = Modifier.width(PickerWidth),
    )
}

@Composable
fun rememberTimeScrollPickerState(
    initialTime: LocalTime = LocalTime(0, 0),
    is24Hour: Boolean = DateFormat.is24HourFormat(LocalContext.current),
): TimeScrollPickerState {
    val group = rememberScrollPickerGroupState()
    val hourState = rememberScrollPickerState(initialIndex = initialTime.hour, group = group)
    val minuteState = rememberScrollPickerState(initialIndex = initialTime.minute, group = group)
    val amPmState = rememberScrollPickerState(initialIndex = initialTime.hour / 12, group = group)

    return remember(is24Hour, hourState, minuteState, amPmState) {
        TimeScrollPickerState(is24Hour, hourState, minuteState, amPmState)
    }
}

/** Both clocks' hour wheels share [hourState], whose index is the hour of the day. */
@Stable
class TimeScrollPickerState(
    internal val is24Hour: Boolean,
    internal val hourState: ScrollPickerState,
    internal val minuteState: ScrollPickerState,
    internal val amPmState: ScrollPickerState,
) {
    val hour: Int get() = hourState.selectedIndex
    val minute: Int get() = minuteState.selectedIndex
    val time: LocalTime get() = LocalTime(hour, minute)

    /** Sets the time of both [hourState] and [minuteState] automatically. [amPmState] is updated by [keepAmPmSyncedWithHourHalfLoop] */
    fun select(time: LocalTime) {
        hourState.selectedIndex = time.hour
        minuteState.selectedIndex = time.minute
    }
}

/** Shows the hour's half of the day on the AM/PM wheel, and moves the hour by 12 when AM/PM is picked. */
private suspend fun TimeScrollPickerState.keepAmPmSyncedWithHourHalfLoop() = coroutineScope {
    launch { snapshotFlow { hour / 12 }.collect { amPmState.selectedIndex = it } }
    launch {
        snapshotFlow { amPmState.selectedIndex }.collect {
            // Lands on the same label, so the jump doesn't show.
            if (it != hour / 12) hourState.select(hour % 12 + 12 * it)
        }
    }
}

private fun twoDigits(number: Int) = number.toString().padStart(2, '0')

/**
 * @param hour Hour value represented as 24-hour format
 */
private fun convert24HoursItemsTo12HourLabels(hour: Int): String = twoDigits((hour + 11) % 12 + 1)

@Preview
@Composable
private fun Preview24() {
    MaterialTheme {
        Scaffold {
            TimeScrollPicker(
                rememberTimeScrollPickerState(LocalTime(10, 30), is24Hour = true),
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
                val state = rememberTimeScrollPickerState(LocalTime(10, 30), is24Hour = false)

                Text(LocalTime(state.hour, state.minute).toString(), style = MaterialTheme.typography.titleLarge)

                TimeScrollPicker(state)
            }
        }
    }
}
