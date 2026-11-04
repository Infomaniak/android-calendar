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
package com.infomaniak.calendar.components.eventdetail.form.component.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.form.ReminderUnit
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.ChevronDown
import com.infomaniak.designsystem.icons.symbols.ChevronUp

private const val AMOUNT_MAX_LENGTH = 3

private val amountInputTransformation = InputTransformation {
    if (!asCharSequence().all(Char::isDigit)) revertAllChanges()
}.then(InputTransformation.maxLength(AMOUNT_MAX_LENGTH))

@Composable
internal fun ReminderOffsetSection(
    amountTextFieldState: TextFieldState,
    amount: Int,
    selectedUnit: ReminderUnit,
    onUnitSelect: (ReminderUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SectionTitle(stringResource(R.string.reminderTitle))

        Row(horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md)) {
            OutlinedTextField(
                state = amountTextFieldState,
                inputTransformation = amountInputTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                lineLimits = TextFieldLineLimits.SingleLine,
                modifier = Modifier.width(72.dp),
            )

            ReminderUnitDropdown(
                selectedUnit = selectedUnit,
                amount = amount,
                onUnitSelect = onUnitSelect,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ReminderUnitDropdown(
    selectedUnit: ReminderUnit,
    amount: Int,
    onUnitSelect: (ReminderUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = reminderUnitLabel(selectedUnit, amount),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                Icon(if (isExpanded) EsdsIcons.ChevronUp.Small else EsdsIcons.ChevronDown.Small, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            containerColor = MenuDefaults.groupStandardContainerColor,
            shape = MenuDefaults.standaloneGroupShape,
        ) {
            ReminderUnit.entries.forEachIndexed { index, unit ->
                SelectableDropdownMenuItem(
                    selected = unit == selectedUnit,
                    onClick = {
                        onUnitSelect(unit)
                        isExpanded = false
                    },
                    text = { Text(reminderUnitLabel(unit, amount)) },
                    shapes = MenuDefaults.itemShape(index, ReminderUnit.entries.size),
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

/** `Minutes before`, agreeing with [amount]. */
@Composable
private fun reminderUnitLabel(unit: ReminderUnit, amount: Int): String {
    val locale = LocalLocale.current.platformLocale
    return stringResource(R.string.notificationTimeBefore, pluralStringResource(unit.labelRes, amount))
        .replaceFirstChar { it.titlecase(locale) }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            ReminderOffsetSection(
                amountTextFieldState = rememberTextFieldState("30"),
                amount = 30,
                selectedUnit = ReminderUnit.Minutes,
                onUnitSelect = {},
            )
        }
    }
}
