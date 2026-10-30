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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.form.ReminderUnit
import com.infomaniak.calendar.components.eventdetail.form.component.AmountTextField
import com.infomaniak.calendar.components.eventdetail.form.component.OptionsDropdown
import com.infomaniak.calendar.components.eventdetail.form.component.SectionTitle
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme

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
            AmountTextField(amountTextFieldState)

            OptionsDropdown(
                options = ReminderUnit.entries,
                selectedOption = selectedUnit,
                onOptionSelect = onUnitSelect,
                optionText = { unit -> reminderUnitLabel(unit, amount) },
                modifier = Modifier.weight(1f),
            )
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
