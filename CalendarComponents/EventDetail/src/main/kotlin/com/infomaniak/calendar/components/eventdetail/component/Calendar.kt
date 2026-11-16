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
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
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
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIcon
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendarGroups
import com.infomaniak.calendar.components.eventdetail.theme.CustomColors
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Calendar

@Composable
internal fun Calendar(calendarColor: Color, calendarName: String, shapes: ListItemShapes, modifier: Modifier = Modifier) {
    SegmentedListItem(
        shapes = shapes,
        colors = CustomColors.EventFormItemColors,
        content = { Text(stringResource(id = R.string.sectionCalendarHeader)) },
        leadingContent = { ItemIcon(EsdsIcons.Calendar) },
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
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
) {
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }

    DropDownItem(
        onClick = { isDialogVisible = true },
        content = { Text(stringResource(id = R.string.sectionCalendarHeader)) },
        leadingContent = { ItemIcon(EsdsIcons.Calendar) },
        trailingContent = {
            CalendarDot(selectedCalendar.color)
            Text(text = selectedCalendar.name, style = MaterialTheme.typography.labelLarge)
        },
        itemCount = calendars.sumOf { it.calendars.size },
        shapes = shapes,
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
            Calendar(
                calendarColor = previewEventDetailCalendar.color,
                calendarName = previewEventDetailCalendar.name,
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewEditable() {
    MaterialTheme {
        Surface {
            CalendarEditable(
                selectedCalendar = previewEventDetailCalendar,
                calendars = previewEventDetailCalendarGroups,
                {},
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
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
