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
package com.infomaniak.calendar.ui.screen.home

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.di.ComposeAppGraph
import com.infomaniak.calendar.ui.LocalUser
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.utils.AccountUtils
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.SortedMap

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    accountUtils: AccountUtils = ComposeAppGraph.accountUtils,
) {
    val scope = rememberCoroutineScope()

    // TODO: Expose a SnapshotStateMap to avoid recomposing everything each time any value is updated in the list of all events
    val events: SortedMap<YearMonth, SortedMap<Int, List<Event>>> by viewModel.events.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        events = { events },
        onDisconnect = {
            scope.launch {
                accountUtils.removeUser(accountUtils.currentUserIdFlow.first() ?: return@launch)
            }
        },
    )
}

@Composable
private fun HomeScreen(
    events: () -> SortedMap<YearMonth, SortedMap<Int, List<Event>>>,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { Text("HomeScreen") },
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("User: ${LocalUser.current?.displayName}")
            Button(onClick = onDisconnect) { Text("Disconnect") }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                events().forEach { (yearMonth, days) ->
                    item(key = yearMonth) { Text("YearMonth: $yearMonth") }

                    days.forEach { (day, events) ->
                        item(key = DayKey(day, yearMonth)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                DayIndicator(day)
                                EventList(events, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayIndicator(day: Int?) {
    Text("Day: $day")
}

@Composable
private fun EventList(events: List<Event>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        events.forEach { event ->
            Event(event)
        }
    }
}

@Composable
private fun Event(event: Event) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(Color.Gray)
            .padding(vertical = 150.dp, horizontal = 40.dp),
    ) {
        Text(event.title)
    }
}

@Parcelize
private data class DayKey(val day: Int, val yearMonth: YearMonth) : Parcelable

@Parcelize
data class YearMonth(val year: Int, val month: Int) : Parcelable, Comparable<YearMonth> {
    override fun compareTo(other: YearMonth): Int {
        return when {
            year != other.year -> year - other.year
            else -> month - other.month
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() = CalendarThemeForPreview {
    HomeScreen(events = { fakeEvents }, onDisconnect = {})
}
