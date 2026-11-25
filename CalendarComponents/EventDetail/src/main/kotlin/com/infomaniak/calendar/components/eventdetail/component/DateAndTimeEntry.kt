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
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.OutlinedToggleButtonDefaults
import androidx.compose.material3.SegmentedListItem
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.infomaniak.calendar.components.eventdetail.component.items.DropDownItem
import com.infomaniak.calendar.components.eventdetail.state.DateAndTimeState
import com.infomaniak.calendar.components.eventdetail.theme.CustomColors
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatCityName
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatExplicitTimeLabel
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatShortDateWithYear
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Globe
import com.infomaniak.scrollpicker.timepicker.TimeScrollPicker
import com.infomaniak.scrollpicker.timepicker.rememberTimeScrollPickerState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
internal fun DateAndTimeEntry(
    label: String,
    entry: DateAndTimeState.Entry,
    isAllDay: () -> Boolean,
    selectedButton: () -> EntryButton?,
    onSelectButton: (EntryButton?) -> Unit,
    onDatePicked: (LocalDate) -> Unit,
    onTimePicked: (LocalTime) -> Unit,
    onTimeZoneClick: () -> Unit,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        shapes = shapes,
        colors = CustomColors.EventFormItemColors,
        modifier = modifier,
        contentPadding = PaddingValues(),
        content = {
            DateAndTimeEntryContent(
                label = label,
                entry = entry,
                isAllDay = isAllDay,
                selectedButton = selectedButton,
                onSelectButton = onSelectButton,
                onTimeZoneClick = onTimeZoneClick,
                onDatePicked = onDatePicked,
                onTimePicked = onTimePicked,
                contentPadding = ListItemDefaults.ContentPadding,
            )
        },
    )
}

@Composable
private fun DateAndTimeEntryContent(
    label: String,
    entry: DateAndTimeState.Entry,
    isAllDay: () -> Boolean,
    selectedButton: () -> EntryButton?,
    onSelectButton: (EntryButton?) -> Unit,
    onTimeZoneClick: () -> Unit,
    onDatePicked: (LocalDate) -> Unit,
    onTimePicked: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
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

    val horizontalContentPadding = contentPadding.onlyHorizontal()
    val bottomContentPadding = contentPadding.calculateBottomPadding()

    Column(modifier = modifier.padding(top = contentPadding.calculateTopPadding())) {
        val isTimeSelected = selectedButton() == EntryButton.Time

        Row(
            horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontalContentPadding),
        ) {
            Text(label)

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(onClick = { isDatePickerVisible = true }, shapes = shapes) {
                Text(entry.date.formatShortDateWithYear())
            }

            AnimatedVisibility(!isAllDay(), enter = enter, exit = exit) {
                OutlinedToggleButton(
                    checked = isTimeSelected,
                    onCheckedChange = { isChecked -> onSelectButton(if (isChecked) EntryButton.Time else null) },
                    shapes = toggleShapes,
                    colors = toggleColors,
                ) {
                    Text(entry.time.formatExplicitTimeLabel())
                }
            }
        }

        AnimatedVisibility(isTimeSelected) {
            val bringIntoViewRequester = remember { BringIntoViewRequester() }
            // Waits for the expansion to end, so the whole picker is scrolled into view
            LaunchedEffect(transition.currentState) {
                if (transition.currentState == EnterExitState.Visible) bringIntoViewRequester.bringIntoView()
            }

            Column(
                modifier = Modifier
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .fillMaxWidth()
                    .padding(top = EsdsTheme.spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimeScrollPicker(
                    entry.timeState,
                    modifier = Modifier.padding(horizontalContentPadding),
                    onTimeChange = onTimePicked,
                )
                TimeZoneButton(
                    entry = entry,
                    onClick = onTimeZoneClick,
                    contentPadding = horizontalContentPadding + timeZoneVerticalPadding(bottomContentPadding),
                )
            }
        }

        // The bottom content padding is either displayed by this spacer or the TimeZoneButton depending on which is the visible one of the two
        AnimatedVisibility(!isTimeSelected) {
            Spacer(modifier = Modifier.height(bottomContentPadding))
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

// Default top padding of the timezone item + custom bottom padding of the parent
private fun timeZoneVerticalPadding(bottomContentPadding: Dp): PaddingValues = PaddingValues(
    bottom = bottomContentPadding,
    top = ListItemDefaults.ContentPadding.calculateTopPadding(),
)

@Composable
private fun TimeZoneButton(
    entry: DateAndTimeState.Entry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = ListItemDefaults.ContentPadding,
) {
    DropDownItem(
        content = { Text(stringResource(R.string.timeZoneLabel)) },
        trailingContent = { Text(entry.timeZone.formatCityName()) },
        onClick = onClick,
        itemCount = Int.MAX_VALUE, // More than one
        modifier = modifier,
        symbol = EsdsIcons.Globe,
        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
        contentPadding = contentPadding,
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
                label = "Start",
                entry = DateAndTimeState.Entry(
                    date = remember { mutableStateOf(LocalDate(2026, 10, 20)) },
                    timeState = rememberTimeScrollPickerState(LocalTime(14, 41)),
                    timeZone = remember { mutableStateOf(TimeZone.UTC) },
                ),
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
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
