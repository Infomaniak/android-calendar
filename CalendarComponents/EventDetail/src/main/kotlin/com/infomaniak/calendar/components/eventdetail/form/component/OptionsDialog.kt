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
package com.infomaniak.calendar.components.eventdetail.form.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.designsystem.core.theme.EsdsTheme

/** Radio options followed by a [customLabel] option opening a screen to make one that isn't listed. */
@Composable
internal fun <T> OptionsDialog(
    options: List<T>,
    selectedOption: T,
    onOptionSelect: (T) -> Unit,
    customLabel: String,
    onCustomClick: () -> Unit,
    onDismissRequest: () -> Unit,
    optionContent: @Composable (T) -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(
                Modifier
                    .selectableGroup()
                    .verticalScroll(rememberScrollState())
                    .padding(EsdsTheme.spacing.md),
            ) {
                val colors = ListItemDefaults.colors(containerColor = AlertDialogDefaults.containerColor)

                options.forEach { option ->
                    val isSelected = option == selectedOption

                    ListItem(
                        selected = isSelected,
                        onClick = { onOptionSelect(option) },
                        trailingContent = { RadioButton(selected = isSelected, onClick = null) },
                        colors = colors,
                    ) {
                        optionContent(option)
                    }
                }

                ListItem(onClick = onCustomClick, colors = colors) {
                    Text(customLabel)
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        OptionsDialog(
            options = listOf("First", "Second", "Third"),
            selectedOption = "Second",
            onOptionSelect = {},
            customLabel = "Custom",
            onCustomClick = {},
            onDismissRequest = {},
            optionContent = { Text(it) },
        )
    }
}
