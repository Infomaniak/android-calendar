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
package com.infomaniak.calendar.components.eventdetail.component.calendar

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIconDefaults
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendarGroups
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.parcelize.Parcelize

@Composable
internal fun CalendarChoiceDialog(
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
                .padding(EsdsTheme.spacing.threeXl)
                .selectableGroup(),
        ) {
            calendarGroups.forEachIndexed { index, (account, calendars) ->
                item(key = Key.Title(account.id)) {
                    val topPadding = if (index == 0) 0.dp else EsdsTheme.spacing.threeXl
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .padding(top = topPadding, bottom = EsdsTheme.spacing.md)
                            .semantics { heading() },
                    )
                }

                calendars.forEach { calendar ->
                    item(key = Key.Calendar(calendar.id)) {
                        CalendarChoiceItem(
                            color = calendar.color,
                            name = calendar.name,
                            isSelected = calendar.id == selectedCalendar.id,
                            onClick = { onSelect(calendar) },
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun CalendarChoiceItem(color: Color, name: String, isSelected: Boolean, onClick: () -> Unit) {
    ListItem(
        leadingContent = { CalendarDot(color) },
        content = { Text(name) },
        trailingContent = { RadioButton(selected = isSelected, onClick = null) },
        selected = isSelected,
        onClick = onClick,
        shapes = ListItemDefaults.shapes(shape = MaterialTheme.shapes.large),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
internal fun CalendarDot(calendarColor: Color) {
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
private fun PreviewChoiceItem() {
    MaterialTheme {
        Surface {
            CalendarChoiceItem(
                color = previewEventDetailCalendar.color,
                name = previewEventDetailCalendar.name,
                isSelected = true,
                onClick = {},
            )
        }
    }
}


@Preview(showSystemUi = true)
@Composable
private fun Preview() {
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
