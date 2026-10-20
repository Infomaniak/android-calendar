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

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.R as RCore

@Composable
fun DeleteSingleEventDialog(onDelete: () -> Unit, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.deleteEventAlertTitle)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDelete()
                    onDismissRequest()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(stringResource(RCore.string.deleteButton))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(RCore.string.buttonCancel)) }
        },
    )
}

@Preview(showSystemUi = true)
@Composable
private fun DeleteSingleEventDialogPreview() {
    CalendarThemeForPreview {
        DeleteSingleEventDialog(onDelete = {}, onDismissRequest = {})
    }
}
