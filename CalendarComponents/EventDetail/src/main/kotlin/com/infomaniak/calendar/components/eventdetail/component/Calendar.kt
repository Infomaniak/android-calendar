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

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.infomaniak.calendar.components.eventdetail.component.items.DropDownItem
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIconDefaults
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendarGroups
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.parcelize.Parcelize

@Composable
internal fun Calendar(calendarColor: Color, calendarName: String, modifier: Modifier = Modifier) {
    ListItem(
        content = { Text(stringResource(id = R.string.sectionCalendarHeader)) },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md),
            ) {
                Dot(calendarColor)
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
            Dot(selectedCalendar.color)
            Text(text = selectedCalendar.name, style = MaterialTheme.typography.labelLarge)
        },
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

@Composable
private fun CalendarChoiceDialog(
    calendarGroups: List<EventDetailCalendarGroup>,
    selectedCalendar: EventDetailCalendar,
    onSelect: (EventDetailCalendar) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        LazyColumn(
            modifier
                .background(AlertDialogDefaults.containerColor, AlertDialogDefaults.shape)
                .padding(EsdsTheme.spacing.threeXl),
        ) {
            calendarGroups.forEachIndexed { index, (account, calendars) ->
                item(key = Key.Title(account.id)) {
                    val topPadding = if (index == 0) 0.dp else EsdsTheme.spacing.threeXl
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = topPadding, bottom = EsdsTheme.spacing.md),
                    )
                }

                calendars.forEach { calendar ->
                    item(key = Key.Calendar(calendar.id)) {
                        CalendarChoiceItem(
                            color = calendar.color,
                            name = calendar.name,
                            isSelected = { calendar.id == selectedCalendar.id },
                            onCheckedChange = { onSelect(calendar) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarChoiceItem(color: Color, name: String, isSelected: () -> Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        leadingContent = { Dot(color) },
        content = { Text(name) },
        trailingContent = { RadioButton(selected = isSelected(), onClick = null) },
        checked = isSelected(),
        onCheckedChange = onCheckedChange,
        shapes = ListItemDefaults.shapes(shape = MaterialTheme.shapes.large),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun Dot(calendarColor: Color) {
    Box(
        modifier = Modifier
            .size(ItemIconDefaults.size)
            .padding(1.dp)
            .clip(CircleShape)
            .background(calendarColor),
    )
}

private sealed interface Key : Parcelable {
    @Parcelize
    data class Title(val id: Int) : Key
    @Parcelize
    data class Calendar(val id: String) : Key
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

@Preview
@Composable
private fun PreviewChoiceItem() {
    MaterialTheme {
        Surface {
            CalendarChoiceItem(
                color = previewEventDetailCalendar.color,
                name = previewEventDetailCalendar.name,
                isSelected = { true },
                onCheckedChange = {},
            )
        }
    }
}
