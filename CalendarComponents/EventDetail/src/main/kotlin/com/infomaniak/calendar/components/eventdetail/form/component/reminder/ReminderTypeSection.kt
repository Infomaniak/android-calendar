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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.form.component.SectionTitle
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.resources.R

private val reminderTypes = listOf(EventDetailUi.Notification.Type.Push, EventDetailUi.Notification.Type.Email)

@Composable
internal fun ReminderTypeSection(
    selectedType: EventDetailUi.Notification.Type,
    onTypeSelect: (EventDetailUi.Notification.Type) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SectionTitle(stringResource(R.string.otherReminderAlarmKindLabel))

        Column(Modifier.selectableGroup()) {
            reminderTypes.forEach { type ->
                val isSelected = selectedType == type

                ListItem(
                    selected = isSelected,
                    onClick = { onTypeSelect(type) },
                    leadingContent = { RadioButton(selected = isSelected, onClick = null) },
                ) {
                    Text(stringResource(type.label))
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            ReminderTypeSection(selectedType = EventDetailUi.Notification.Type.Push, onTypeSelect = {})
        }
    }
}
