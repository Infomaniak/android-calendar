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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.ui.screen.calendarTest.CalendarTestUiState
import com.infomaniak.calendar.ui.screen.calendarTest.previewParameter.CalendarTestUiStatePreviewProvider

@Composable
internal fun Loaded(state: CalendarTestUiState.Loaded) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "CalDAV PoC – Rust Bridge",
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        if (state.calendars.isEmpty()) {
            item { Text("⏳ Syncing…") }
        }

        items(state.calendars, key = { it.id }) { calendar ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = calendar.header,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (calendar.events.isEmpty()) {
                    Text(
                        text = "No event",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(calendar.events, key = { it.id }) { event -> EventCard(event) }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun LoadedPreview() {
    Loaded(state = CalendarTestUiStatePreviewProvider.Loaded)
}
