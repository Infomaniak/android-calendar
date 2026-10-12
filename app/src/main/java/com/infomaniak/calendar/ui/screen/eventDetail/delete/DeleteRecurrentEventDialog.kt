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
package com.infomaniak.calendar.ui.screen.eventDetail.delete

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.R
import com.infomaniak.core.common.R as RCore

enum class DeleteEventScope(@StringRes val labelRes: Int) {
    ThisEvent(R.string.buttonDeleteEvent),
    ThisAndFollowingEvents(R.string.buttonDeleteThisAndFollowingEvents),
    AllEvents(R.string.buttonDeleteAllEvents),
}

@Composable
fun DeleteRecurrentEventDialog(
    onDelete: (DeleteEventScope) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedScope by rememberSaveable { mutableStateOf(DeleteEventScope.ThisEvent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.deleteRecurringEventAlertTitle)) },
        text = {
            Column(Modifier.selectableGroup()) {
                DeleteEventScope.entries.forEach { scope ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedScope == scope,
                                onClick = { selectedScope = scope },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedScope == scope,
                            onClick = null,
                        )
                        Text(
                            text = stringResource(scope.labelRes),
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDelete(selectedScope)
                    onDismiss()
                },
            ) {
                Text(
                    stringResource(R.string.buttonDeleteEvent),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(RCore.string.buttonCancel))
            }
        },
    )
}
