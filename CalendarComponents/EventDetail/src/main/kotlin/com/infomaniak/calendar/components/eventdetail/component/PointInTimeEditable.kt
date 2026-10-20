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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.state.DateAndTimeState
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatCityName
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatExplicitTimeLabel
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatShortDateWithYear
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
internal fun PointInTimeEditable(
    pointInTime: DateAndTimeState.PointInTime,
    modifier: Modifier = Modifier,
) {
    val shapes = ButtonDefaults.shapes(MaterialTheme.shapes.medium, CircleShape)
    var isDatePickerVisible by rememberSaveable { mutableStateOf(false) }
    var isTimePickerVisible by rememberSaveable { mutableStateOf(false) }

    Row(horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.lg), modifier = modifier) {
        OutlinedButton(onClick = { isDatePickerVisible = true }, shapes = shapes) {
            Text(pointInTime.date.formatShortDateWithYear())
        }

        OutlinedButton(onClick = { isTimePickerVisible = true }, shapes = shapes) {
            Text(pointInTime.time.formatExplicitTimeLabel())
        }

        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            TextButton(
                onClick = {},
                colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current),
                shapes = shapes,
            ) {
                Icon(
                    painterResource(R.drawable.ic_globe),
                    contentDescription = null,
                    modifier = Modifier.size(EsdsTheme.icon.sizeSm),
                )
                Spacer(modifier = Modifier.width(EsdsTheme.spacing.md))
                Text(pointInTime.timeZone.formatCityName(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }

    if (isDatePickerVisible) {
        DateSelectionDialog(
            initialDate = pointInTime.date,
            onDateSelected = { pointInTime.date = it },
            onDismissRequest = { isDatePickerVisible = false },
        )
    }

    if (isTimePickerVisible) {
        TimeSelectionDialog(
            initialTime = pointInTime.time,
            onTimeSelected = { pointInTime.time = it },
            onDismissRequest = { isTimePickerVisible = false },
        )
    }
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
private fun TimeSelectionDialog(initialTime: LocalTime, onTimeSelected: (LocalTime) -> Unit, onDismissRequest: () -> Unit) {
    val state = rememberTimePickerState(initialHour = initialTime.hour, initialMinute = initialTime.minute)

    TimePickerDialog(
        onDismissRequest = onDismissRequest,
        title = {},
        confirmButton = {
            ConfirmButton {
                onTimeSelected(LocalTime(state.hour, state.minute))
                onDismissRequest()
            }
        },
        dismissButton = { CancelButton(onDismissRequest) },
    ) {
        TimePicker(state)
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
            PointInTimeEditable(
                pointInTime = DateAndTimeState.PointInTime(
                    date = remember { mutableStateOf(LocalDate(2026, 10, 20)) },
                    time = remember { mutableStateOf(LocalTime(14, 41)) },
                    timeZone = remember { mutableStateOf(TimeZone.UTC) },
                ),
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

@Preview(showSystemUi = true)
@Composable
private fun TimeSelectionDialogPreview() {
    MaterialTheme {
        TimeSelectionDialog(
            initialTime = LocalTime(14, 41),
            onTimeSelected = {},
            onDismissRequest = {},
        )
    }
}
