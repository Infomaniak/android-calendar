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

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview

@Composable
fun DeleteEventDialog(isRecurrent: () -> Boolean, onDelete: (DeleteEventScope) -> Unit, onDismiss: () -> Unit) {
    if (isRecurrent()) {
        DeleteRecurrentEventDialog(onDelete = onDelete, onDismiss = onDismiss)
    } else {
        DeleteSingleEventDialog(onDelete = { onDelete(DeleteEventScope.ThisEvent) }, onDismiss = onDismiss)
    }
}

@Preview(showSystemUi = true)
@Composable
private fun DeleteEventDialogSinglePreview() {
    CalendarThemeForPreview {
        DeleteEventDialog(isRecurrent = { false }, onDelete = {}, onDismiss = {})
    }
}

@Preview(showSystemUi = true)
@Composable
private fun DeleteEventDialogRecurrentPreview() {
    CalendarThemeForPreview {
        DeleteEventDialog(isRecurrent = { true }, onDelete = {}, onDismiss = {})
    }
}
