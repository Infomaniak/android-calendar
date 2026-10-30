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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.form.component.OptionsDialog
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi
import com.infomaniak.calendar.components.resources.R
import kotlinx.datetime.LocalDateTime

private val recurrencePresets = listOf(RecurrenceUi.None) + RecurrenceUi.Rule.presets

/** Lists the presets, plus [selectedRecurrence] when it isn't one of them. */
@Composable
internal fun RecurrenceDialog(
    selectedRecurrence: RecurrenceUi,
    start: LocalDateTime,
    getRecurrenceText: suspend (RecurrenceUi, start: LocalDateTime) -> String?,
    onRecurrenceSelect: (RecurrenceUi) -> Unit,
    onCustomClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    OptionsDialog(
        options = if (selectedRecurrence in recurrencePresets) recurrencePresets else recurrencePresets + selectedRecurrence,
        selectedOption = selectedRecurrence,
        onOptionSelect = onRecurrenceSelect,
        customLabel = stringResource(R.string.customRecurrenceLabel),
        onCustomClick = onCustomClick,
        onDismissRequest = onDismissRequest,
    ) { recurrence ->
        Text(recurrenceText(recurrence, start, getRecurrenceText).orEmpty())
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        RecurrenceDialog(
            selectedRecurrence = RecurrenceUi.None,
            start = LocalDateTime(2026, 12, 21, 9, 30),
            getRecurrenceText = { recurrence, _ -> (recurrence as? RecurrenceUi.Rule)?.frequency?.name },
            onRecurrenceSelect = {},
            onCustomClick = {},
            onDismissRequest = {},
        )
    }
}
