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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.ui.screen.calendarTest.CalendarTestUiState
import com.infomaniak.calendar.ui.screen.calendarTest.paging.ScrollInfo
import com.infomaniak.calendar.ui.screen.calendarTest.paging.ScrollPositionEffect
import com.infomaniak.calendar.ui.screen.calendarTest.previewParameter.CalendarTestUiStatePreviewProvider
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun Planning(
    state: CalendarTestUiState.Loaded,
    onScroll: (ScrollInfo) -> Unit = {},
) {
    if (state.weeks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No upcoming event", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.weeks.forEach { week ->
            stickyHeader(key = "week-${week.id}") { PlanningWeekHeader(title = week.header) }
            week.days.forEach { day ->
                item(key = "day-${day.id}") { PlanningDayHeader(title = day.header) }
                items(day.events, key = { event -> "event-${event.id}" }) { event -> EventCard(event) }
            }
        }
    }

    ScrollPositionEffect(listState = listState, onScroll = onScroll)
}

@Composable
@Preview
private fun PlanningPreview() = CalendarThemeForPreview {
    Planning(state = CalendarTestUiStatePreviewProvider.Loaded)
}



