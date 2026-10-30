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
package com.infomaniak.calendar.components.eventdetail.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.form.component.reminder.ReminderOffsetSection
import com.infomaniak.calendar.components.eventdetail.form.component.reminder.ReminderTypeSection
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
fun CustomReminder(state: CustomReminderState, modifier: Modifier = Modifier, contentPadding: PaddingValues = PaddingValues()) {
    Column(
        verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xl),
        modifier = modifier.padding(contentPadding),
    ) {
        ReminderOffsetSection(
            amountTextFieldState = state.amountTextFieldState,
            amount = state.amount ?: 0,
            selectedUnit = state.unit.value,
            onUnitSelect = { unit -> state.unit.value = unit },
        )

        ReminderTypeSection(
            selectedType = state.type.value,
            onTypeSelect = { type -> state.type.value = type },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            CustomReminder(state = rememberCustomReminderState(), contentPadding = PaddingValues(16.dp))
        }
    }
}
