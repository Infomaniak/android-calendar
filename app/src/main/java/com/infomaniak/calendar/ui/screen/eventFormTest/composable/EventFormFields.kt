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
package com.infomaniak.calendar.ui.screen.eventFormTest.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.ui.screen.eventFormTest.EventFormAction
import com.infomaniak.calendar.ui.screen.eventFormTest.EventFormAction.OnAllDayChange
import com.infomaniak.calendar.ui.screen.eventFormTest.EventFormAction.OnCalendarChange
import com.infomaniak.calendar.ui.screen.eventFormTest.EventFormAction.OnDescriptionChange
import com.infomaniak.calendar.ui.screen.eventFormTest.EventFormAction.OnEndChange
import com.infomaniak.calendar.ui.screen.eventFormTest.EventFormAction.OnLocationChange
import com.infomaniak.calendar.ui.screen.eventFormTest.EventFormAction.OnStartChange
import com.infomaniak.calendar.ui.screen.eventFormTest.EventFormAction.OnTitleChange
import com.infomaniak.calendar.ui.screen.eventFormTest.EventFormUiState
import com.infomaniak.calendar.ui.screen.eventFormTest.model.CalendarChoice
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId

@Composable
internal fun EventFormFields(
    state: EventFormUiState.Editing,
    processAction: (EventFormAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val form = state.form
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = form.title,
            onValueChange = { processAction(OnTitleChange(it)) },
            label = { Text("Titre") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Toute la journée")
            Switch(checked = form.isAllDay, onCheckedChange = { processAction(OnAllDayChange(it)) })
        }

        DateTimeField("Début", form.start, showTime = !form.isAllDay) { processAction(OnStartChange(it)) }
        DateTimeField("Fin", form.end, showTime = !form.isAllDay) { processAction(OnEndChange(it)) }

        OutlinedTextField(
            value = form.location,
            onValueChange = { processAction(OnLocationChange(it)) },
            label = { Text("Lieu") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.description,
            onValueChange = { processAction(OnDescriptionChange(it)) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
        )

        CalendarDropdown(
            calendars = state.calendars,
            selectedId = form.calendarId.url,
            onSelect = { processAction(OnCalendarChange(it)) },
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarDropdown(
    calendars: List<CalendarChoice>,
    selectedId: String,
    onSelect: (CalendarId) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = calendars.firstOrNull { it.id.url == selectedId }?.name.orEmpty()

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Calendrier") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            calendars.forEach { calendar ->
                DropdownMenuItem(
                    text = { Text(calendar.name) },
                    onClick = {
                        onSelect(calendar.id)
                        expanded = false
                    },
                )
            }
        }
    }
}





