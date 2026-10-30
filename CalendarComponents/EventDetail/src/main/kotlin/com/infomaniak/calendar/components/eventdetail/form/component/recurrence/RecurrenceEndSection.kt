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
package com.infomaniak.calendar.components.eventdetail.form.component.recurrence

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.DateSelectionDialog
import com.infomaniak.calendar.components.eventdetail.form.RecurrenceEndType
import com.infomaniak.calendar.components.eventdetail.form.component.AmountTextField
import com.infomaniak.calendar.components.eventdetail.form.component.OptionsDropdown
import com.infomaniak.calendar.components.eventdetail.form.component.SectionTitle
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatShortDateWithYear
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Calendar
import kotlinx.datetime.LocalDate

@Composable
internal fun RecurrenceEndSection(
    selectedEndType: RecurrenceEndType,
    onEndTypeSelect: (RecurrenceEndType) -> Unit,
    countTextFieldState: TextFieldState,
    untilDate: LocalDate,
    onUntilDateSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SectionTitle(stringResource(R.string.recurrenceEndTitle))

        Row(horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md)) {
            OptionsDropdown(
                options = RecurrenceEndType.entries,
                selectedOption = selectedEndType,
                onOptionSelect = onEndTypeSelect,
                optionText = { endType -> stringResource(endType.labelRes) },
                modifier = Modifier.weight(1f),
            )

            when (selectedEndType) {
                RecurrenceEndType.Never -> Unit
                RecurrenceEndType.AfterCount -> AmountTextField(countTextFieldState)
                RecurrenceEndType.Until -> UntilDateField(untilDate, onUntilDateSelect, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun UntilDateField(date: LocalDate, onDateSelect: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    var isDatePickerVisible by rememberSaveable { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { if (it is PressInteraction.Release) isDatePickerVisible = true }
    }

    OutlinedTextField(
        value = date.formatShortDateWithYear(),
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        trailingIcon = { Icon(EsdsIcons.Calendar.Small, contentDescription = null) },
        interactionSource = interactionSource,
        modifier = modifier,
    )

    if (isDatePickerVisible) {
        DateSelectionDialog(
            initialDate = date,
            onDateSelected = onDateSelect,
            onDismissRequest = { isDatePickerVisible = false },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            RecurrenceEndSection(
                selectedEndType = RecurrenceEndType.Until,
                onEndTypeSelect = {},
                countTextFieldState = rememberTextFieldState("10"),
                untilDate = LocalDate(2026, 12, 21),
                onUntilDateSelect = {},
            )
        }
    }
}
