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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.planning.Planning
import com.infomaniak.calendar.di.ComposeAppGraph
import com.infomaniak.calendar.ui.LocalUser
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.utils.AccountUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.time.format.DateTimeFormatter
import kotlin.time.Clock

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    accountUtils: AccountUtils = ComposeAppGraph.accountUtils,
) {
    val scope = rememberCoroutineScope()

    // TODO: Expose a SnapshotStateMap to avoid recomposing everything each time any value is updated in the list of all events
    val weekEvents: EventsByWeekAndDay by viewModel.weekEvents.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        weekEvents = { weekEvents },
        onDisconnect = {
            scope.launch {
                accountUtils.removeUser(accountUtils.currentUserIdFlow.first() ?: return@launch)
            }
        },
    )
}

@Composable
private fun HomeScreen(
    weekEvents: () -> EventsByWeekAndDay,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Clock.System.todayIn(TimeZone.currentSystemDefault())

    Scaffold(
        topBar = { Text("HomeScreen") },
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("User: ${LocalUser.current?.displayName}")
            Button(onClick = onDisconnect) { Text("Disconnect") }

            Planning(
                weekEvents = weekEvents,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() = CalendarThemeForPreview {
    HomeScreen(weekEvents = { fakeEvents }, onDisconnect = {})
}
