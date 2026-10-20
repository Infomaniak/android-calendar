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
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.R as RCore

enum class DeleteEventScope(@StringRes val labelRes: Int) {
    ThisEvent(R.string.buttonDeleteEvent),
    ThisAndFollowingEvents(R.string.buttonDeleteThisAndFollowingEvents),
    AllEvents(R.string.buttonDeleteAllEvents),
}

@Composable
fun DeleteRecurrentEventDialog(onDelete: (DeleteEventScope) -> Unit, onDismiss: () -> Unit) {
    var selectedScope by rememberSaveable { mutableStateOf(DeleteEventScope.ThisEvent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.deleteRecurringEventAlertTitle)) },
        text = {
            Column(Modifier.selectableGroup()) {
                DeleteEventScope.entries.forEach { scope ->
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
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = Color.Transparent,
                ),
                onClick = {
                    onDelete(selectedScope)
                    onDismiss()
                },
            ) {
                Text(stringResource(RCore.string.deleteButton))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(RCore.string.buttonCancel))
            }
        },
    )
}

@Preview(showSystemUi = true)
@Composable
private fun DeleteRecurrentEventDialogPreview() {
    CalendarThemeForPreview {
        DeleteRecurrentEventDialog(onDelete = {}, onDismiss = {})
    }
}
