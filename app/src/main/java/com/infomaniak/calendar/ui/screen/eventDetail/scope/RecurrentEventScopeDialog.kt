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
package com.infomaniak.calendar.ui.screen.eventDetail.scope

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.core.common.R as RCore

@Composable
fun RecurrentEventScopeDialog(
    title: String,
    confirmLabel: String,
    onConfirm: (RecurrentEventScope) -> Unit,
    onDismissRequest: () -> Unit,
    confirmTextButtonColors: ButtonColors = ButtonDefaults.textButtonColors(),
) {
    var selectedScope by rememberSaveable { mutableStateOf(RecurrentEventScope.ThisEvent) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = {
            Column(Modifier.selectableGroup()) {
                RecurrentEventScope.entries.forEach { scope ->
                    val isSelected = selectedScope == scope

                    ListItem(
                        selected = isSelected,
                        onClick = { selectedScope = scope },
                        leadingContent = { RadioButton(selected = isSelected, onClick = null) },
                        colors = ListItemDefaults.colors(containerColor = AlertDialogDefaults.containerColor),
                    ) {
                        Text(stringResource(scope.labelRes))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                colors = confirmTextButtonColors,
                onClick = {
                    onConfirm(selectedScope)
                    onDismissRequest()
                },
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(RCore.string.buttonCancel))
            }
        },
    )
}

@Preview
@Composable
private fun RecurrentEventScopeDialogPreview() {
    RecurrentEventScopeDialog(
        title = stringResource(R.string.editRecurringEventAlertTitle),
        confirmLabel = stringResource(RCore.string.edit),
        onConfirm = {},
        onDismissRequest = {},
    )
}
