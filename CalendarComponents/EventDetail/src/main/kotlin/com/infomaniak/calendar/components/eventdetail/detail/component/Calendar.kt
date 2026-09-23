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
package com.infomaniak.calendar.components.eventdetail.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
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
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
internal fun Calendar(calendarColor: Color, calendarName: String, modifier: Modifier = Modifier) {
    ListItem(
        content = { Text(stringResource(id = R.string.sectionCalendarHeader)) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(EsdsTheme.icon.sizeMd)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(calendarColor),
            )
        },
        trailingContent = { Text(text = calendarName, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun PreviewCalendar() {
    MaterialTheme {
        Surface {
            Calendar(calendarColor = Color.Blue, calendarName = "Vacation")
        }
    }
}
