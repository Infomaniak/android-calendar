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
package com.infomaniak.calendar.components.eventdetail.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.OutlinedToggleButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButtonSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.items.DropDownItem
import com.infomaniak.calendar.components.eventdetail.state.DateAndTimeState
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatCityName
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatExplicitTimeLabel
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatShortDateWithYear
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Globe
import com.infomaniak.scrollpicker.timepicker.TimeScrollPicker
import com.infomaniak.scrollpicker.timepicker.rememberTimeScrollPickerState
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
internal fun DateAndTimeEntry(
    entry: DateAndTimeState.Entry,
    isAllDay: () -> Boolean,
    selectedButton: () -> EntryButton?,
    onSelectButton: (EntryButton?) -> Unit,
    onDatePicked: (LocalDate) -> Unit,
    onTimePicked: (LocalTime) -> Unit,
    onTimeZoneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeScrollPickerState = rememberTimeScrollPickerState(entry.time)

    var isDatePickerVisible by rememberSaveable { mutableStateOf(false) }

    val shapes = ButtonDefaults.shapes(CircleShape, CircleShape)
    val toggleShapes = OutlinedToggleButtonDefaults.shapesFor(ToggleButtonSize.Small)
    val toggleColors = OutlinedToggleButtonDefaults.colors(
        checkedContainerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = EsdsTheme.opacity.subtle),
        checkedContentColor = MaterialTheme.colorScheme.onSurfaceVariant, // Like contentColor
    )

    val effectSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val enter = fadeIn(effectSpec)
    val exit = fadeOut(effectSpec)

    val currentOnTimePicked by rememberUpdatedState(onTimePicked)
    LaunchedEffect(timeScrollPickerState, entry) {
        launch { snapshotFlow { timeScrollPickerState.time }.collect { if (it != entry.time) currentOnTimePicked(it) } }
        launch { snapshotFlow { entry.time }.collect { if (it != timeScrollPickerState.time) timeScrollPickerState.select(it) } }
    }

    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(horizontal = EsdsTheme.spacing.xl),
        ) {
            Text("Début TODO")

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(onClick = { isDatePickerVisible = true }, shapes = shapes) {
                Text(entry.date.formatShortDateWithYear())
            }

            AnimatedVisibility(!isAllDay(), enter = enter, exit = exit) {
                OutlinedToggleButton(
                    checked = selectedButton() == EntryButton.Time,
                    onCheckedChange = { isChecked -> onSelectButton(if (isChecked) EntryButton.Time else null) },
                    shapes = toggleShapes,
                    colors = toggleColors,
                ) {
                    Text(entry.time.formatExplicitTimeLabel())
                }
            }
        }

        AnimatedVisibility(selectedButton() == EntryButton.Time) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = EsdsTheme.spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimeScrollPicker(timeScrollPickerState)
                TimeZoneButton(entry, onClick = onTimeZoneClick)
            }
        }
    }

    if (isDatePickerVisible) {
        DateSelectionDialog(
            initialDate = entry.date,
            onDateSelected = onDatePicked,
            onDismissRequest = { isDatePickerVisible = false },
        )
    }
}

@Composable
private fun TimeZoneButton(
    entry: DateAndTimeState.Entry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropDownItem(
        content = { Text("Fuseau horaire TODO") },
        trailingContent = { Text(entry.timeZone.formatCityName()) },
        onClick = onClick,
        itemCount = Int.MAX_VALUE, // More than one
        modifier = modifier,
        symbol = EsdsIcons.Globe,
    )
}

@Composable
private fun DateSelectionDialog(initialDate: LocalDate, onDateSelected: (LocalDate) -> Unit, onDismissRequest: () -> Unit) {
    // DatePicker works with UTC midnight millis
    val state = rememberDatePickerState(initialDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ConfirmButton {
                state.selectedDateMillis?.let {
                    onDateSelected(Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date)
                }
                onDismissRequest()
            }
        },
        dismissButton = { CancelButton(onDismissRequest) },
    ) {
        DatePicker(state)
    }
}

@Composable
private fun ConfirmButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text(stringResource(android.R.string.ok)) }
}

@Composable
private fun CancelButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text(stringResource(android.R.string.cancel)) }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            DateAndTimeEntry(
                entry = DateAndTimeState.Entry(
                    date = remember { mutableStateOf(LocalDate(2026, 10, 20)) },
                    time = remember { mutableStateOf(LocalTime(14, 41)) },
                    timeZone = remember { mutableStateOf(TimeZone.UTC) },
                ),
                isAllDay = { false },
                selectedButton = { EntryButton.Time },
                onSelectButton = {},
                onDatePicked = {},
                onTimePicked = {},
                onTimeZoneClick = {},
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun DateSelectionDialogPreview() {
    MaterialTheme {
        DateSelectionDialog(
            initialDate = LocalDate(2026, 10, 20),
            onDateSelected = {},
            onDismissRequest = {},
        )
    }
}
