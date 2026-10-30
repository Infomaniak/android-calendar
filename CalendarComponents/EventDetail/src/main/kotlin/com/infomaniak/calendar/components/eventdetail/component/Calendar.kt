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
package com.infomaniak.calendar.components.eventdetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.calendar.CalendarChoiceDialog
import com.infomaniak.calendar.components.eventdetail.component.calendar.CalendarDot
import com.infomaniak.calendar.components.eventdetail.component.items.DropDownItem
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendarGroups
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
internal fun Calendar(calendarColor: Color, calendarName: String, modifier: Modifier = Modifier) {
    ListItem(
        content = { Text(stringResource(id = R.string.sectionCalendarHeader)) },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md),
            ) {
                CalendarDot(calendarColor)
                Text(text = calendarName, style = MaterialTheme.typography.labelLarge)
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun CalendarEditable(
    selectedCalendar: EventDetailCalendar,
    calendars: List<EventDetailCalendarGroup>,
    onSelect: (EventDetailCalendar) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }

    DropDownItem(
        onClick = { isDialogVisible = true },
        content = { Text(stringResource(id = R.string.sectionCalendarHeader)) },
        trailingContent = {
            CalendarDot(selectedCalendar.color)
            Text(text = selectedCalendar.name, style = MaterialTheme.typography.labelLarge)
        },
        itemCount = calendars.sumOf { it.calendars.size },
        modifier = modifier,
    )

    if (isDialogVisible) {
        CalendarChoiceDialog(
            calendars,
            selectedCalendar,
            onSelect = { calendar ->
                onSelect(calendar)
                isDialogVisible = false
            },
            onDismissRequest = { isDialogVisible = false },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            Calendar(calendarColor = previewEventDetailCalendar.color, calendarName = previewEventDetailCalendar.name)
        }
    }
}

@Preview
@Composable
private fun PreviewEditable() {
    MaterialTheme {
        Surface {
            CalendarEditable(selectedCalendar = previewEventDetailCalendar, calendars = previewEventDetailCalendarGroups, {})
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewCalendarDialog() {
    MaterialTheme {
        Surface {
            CalendarChoiceDialog(
                calendarGroups = previewEventDetailCalendarGroups,
                selectedCalendar = previewEventDetailCalendar,
                onSelect = {},
                onDismissRequest = {},
            )
        }
    }
}
