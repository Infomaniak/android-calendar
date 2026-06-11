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
package com.infomaniak.calendar.ui.screen.calendarTest.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.ui.screen.calendarTest.model.CalendarUi
import com.infomaniak.calendar.ui.screen.calendarTest.previewParameter.CalendarTestUiStatePreviewProvider
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview

@Composable
internal fun CalendarHeader(calendar: CalendarUi) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(calendar.color)),
        )
        Text(
            text = calendar.header,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Text(text = if (calendar.isReadOnly) "\uD83D\uDD12" else "\uD83D\uDD13")
    }
}

@Composable
@Preview
private fun CalendarHeaderPreview() = CalendarThemeForPreview {
    Column {
        CalendarHeader(CalendarTestUiStatePreviewProvider.CalendarUi)
        CalendarHeader(CalendarTestUiStatePreviewProvider.CalendarUi.copy(color = 0xFF4CAF50.toInt(), isReadOnly = true))
    }
}
