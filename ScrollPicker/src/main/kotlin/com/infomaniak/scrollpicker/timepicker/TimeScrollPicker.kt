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
import androidx.annotation.PluralsRes
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.scrollpicker.R
import com.infomaniak.scrollpicker.ScrollPicker
import com.infomaniak.scrollpicker.ScrollPickerDefaults
import com.infomaniak.scrollpicker.ScrollPickerGroupState
import com.infomaniak.scrollpicker.ScrollPickerState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import java.text.DateFormatSymbols

private val PreferredWheelWidth = 110.dp
private val Hours24 = (0..23).toList()

/**
 * Needs a bounded width: its wheels share the width it is given.
 *
 * @param onTimeChange called with each time picked on the wheels, which it commits to [state] itself, like a text field's
 * `onValueChange`, so it may correct or reject it.
 */
@Composable
fun TimeScrollPicker(
    state: TimeScrollPickerState,
    modifier: Modifier = Modifier,
    onTimeChange: (LocalTime) -> Unit = { state.time = it },
    is24Hour: Boolean = DateFormat.is24HourFormat(LocalContext.current),
) {
    val currentOnTimeChange by rememberUpdatedState(onTimeChange)
    LaunchedEffect(state) { state.syncWheels { currentOnTimeChange(it) } }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // Wheels share the width left by the separators, shrinking below PreferredWheelWidth when it runs out.
        val wheelModifier = Modifier
            .weight(1f, fill = false)
            .width(PreferredWheelWidth)

        if (is24Hour) {
            HourPicker24(state, modifier = wheelModifier)
            Text(text = ":", style = ScrollPickerDefaults.textStyle)
            MinutePicker(state, ImeAction.Done, modifier = wheelModifier)
        } else {
            HourPicker12(state, modifier = wheelModifier)
            Text(text = ":", style = ScrollPickerDefaults.textStyle)
            MinutePicker(state, ImeAction.Next, modifier = wheelModifier)
            AmPmPicker(state, modifier = wheelModifier)
        }
    }
}

@Composable
private fun HourPicker24(state: TimeScrollPickerState, modifier: Modifier = Modifier) {
    ScrollPicker(
        items = Hours24,
        state = state.hourState,
        contentDescription = pluralStringResource(R.plurals.unitHours, 1),
        loop = true,
        itemLabel = ::twoDigits,
        itemAccessibilityDescription = quantityDescription(R.plurals.hourAmount),
        onInput = String::toIntOrNull,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = modifier,
    )
}

@Composable
private fun MinutePicker(state: TimeScrollPickerState, imeAction: ImeAction, modifier: Modifier = Modifier) {
    ScrollPicker(
        items = (0..59).toList(),
        state = state.minuteState,
        contentDescription = pluralStringResource(R.plurals.unitMinutes, 1),
        loop = true,
        itemLabel = ::twoDigits,
        itemAccessibilityDescription = quantityDescription(R.plurals.minuteAmount),
        onInput = String::toIntOrNull,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        modifier = modifier,
    )
}

/**
 * Holds the 24 hours of the day, labelled from 12 to 11 twice, so scrolling past 11 moves into the other half. The am/pm is
 * derived from which half of the loop we're on. This lets us keep a single [ScrollPickerState] for both formats.
 */
@Composable
private fun HourPicker12(state: TimeScrollPickerState, modifier: Modifier = Modifier) {
    LaunchedEffect(state) { state.keepAmPmSyncedWithHourHalfLoop() }
    val describeHour = quantityDescription(R.plurals.hourAmount)

    ScrollPicker(
        items = Hours24,
        state = state.hourState,
        contentDescription = pluralStringResource(R.plurals.unitHours, 1),
        loop = true,
        itemLabel = ::convert24HoursItemsTo12HourLabels,
        itemAccessibilityDescription = { describeHour(to12Hour(it)) },
        // Keeps the half of the day already chosen, so typing "12" doesn't flip it.
        onInput = { text -> text.toIntOrNull()?.takeIf { it in 1..12 }?.let { it % 12 + 12 * (state.hour / 12) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = modifier,
    )
}

/** Describes a value as its quantity, e.g. "9 hours". */
@Composable
private fun quantityDescription(@PluralsRes id: Int): (Int) -> String {
    val resources = LocalResources.current
    return remember(resources, id) { { resources.getQuantityString(id, it, it) } }
}

/** Items are 0 for AM and 1 for PM, labelled in the current locale. */
@Composable
private fun AmPmPicker(state: TimeScrollPickerState, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val labels = remember(locale) { DateFormatSymbols.getInstance(locale).amPmStrings.toList() }
    ScrollPicker(
        items = listOf(0, 1),
        state = state.amPmState,
        contentDescription = null,
        itemLabel = labels::get,
        onInput = { text ->
            labels.indexOfFirst { text.isNotEmpty() && it.startsWith(text, ignoreCase = true) }.takeIf { it >= 0 }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        modifier = modifier,
    )
}

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

    private val group = ScrollPickerGroupState()

    /** Shared by both clocks' hour wheels, its index is the hour of the day. */
    internal val hourState = ScrollPickerState(initialIndex = initialTime.hour, group = group)
    internal val minuteState = ScrollPickerState(initialIndex = initialTime.minute, group = group)
    internal val amPmState = ScrollPickerState(initialIndex = initialTime.hour / 12, group = group)

    internal val hour: Int get() = hourState.selectedIndex
    private val minute: Int get() = minuteState.selectedIndex

    /** Moves the wheels to [time] when it's set, and hands the times picked on them to [onPick], while the picker is shown. */
    internal suspend fun syncWheels(onPick: (LocalTime) -> Unit) = coroutineScope {
        // The wheels kept their position while the picker was hidden, so they must show [time] before being observed.
        if (!shows(time)) jumpTo(time)

        launch { snapshotFlow { time }.collect { if (!shows(it)) select(it) } }
        launch {
            snapshotFlow { LocalTime(hour, minute) }.collect {
                if (shows(time)) return@collect
                onPick(it)
                // The pick may be rejected or changed without changing [time], which the first flow wouldn't see.
                if (!shows(time)) select(time)
            }
        }
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
        val Saver = Saver(save = { it.time }, restore = ::TimeScrollPickerState)
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

/** Converts an hour of the day (0 to 23) to its 12-hour clock value (1 to 12). */
private fun to12Hour(hour: Int): Int = (hour + 11) % 12 + 1

/**
 * @param hour Hour value represented as 24-hour format
 */
private fun convert24HoursItemsTo12HourLabels(hour: Int): String = twoDigits(to12Hour(hour))

@Preview
@Composable
private fun Preview24() {
    MaterialTheme {
        Scaffold {
            TimeScrollPicker(
                rememberTimeScrollPickerState(LocalTime(10, 30)),
                is24Hour = true,
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
                val state = rememberTimeScrollPickerState(LocalTime(10, 30))

                Text(state.time.toString(), style = MaterialTheme.typography.titleLarge)

                TimeScrollPicker(state, is24Hour = false)
            }
        }
    }
}

@Preview(widthDp = 240)
@Composable
private fun Preview12Narrow() {
    MaterialTheme {
        Scaffold {
            TimeScrollPicker(
                rememberTimeScrollPickerState(LocalTime(10, 30)),
                modifier = Modifier
                    .padding(it)
                    .padding(top = 56.dp),
                is24Hour = false,
            )
        }
    }
}
