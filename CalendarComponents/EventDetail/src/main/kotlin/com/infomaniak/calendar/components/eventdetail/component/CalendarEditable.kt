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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.component.items.DropDownItem
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIconDefaults
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.components.resources.R

@Composable
internal fun CalendarEditable(
    eventDetailCalendar: EventDetailCalendar,
    calendars: List<EventDetailCalendar>,
    modifier: Modifier = Modifier,
) {
    DropDownItem(
        onClick = {},
        content = { Text(stringResource(id = R.string.sectionCalendarHeader)) },
        trailingContent = {
            Dot(eventDetailCalendar.color)
            Text(text = eventDetailCalendar.name, style = MaterialTheme.typography.labelLarge)
        },
        modifier = modifier,
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

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            CalendarEditable(eventDetailCalendar = previewEventDetailCalendar, calendars = listOf(previewEventDetailCalendar))
        }
    }
}
