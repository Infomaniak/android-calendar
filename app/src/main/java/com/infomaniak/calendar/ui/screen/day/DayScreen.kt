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
package com.infomaniak.calendar.ui.screen.day

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.infomaniak.calendar.di.ComposeAppGraph
import com.infomaniak.calendar.ui.LocalUser
import com.infomaniak.calendar.utils.AccountUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(modifier: Modifier = Modifier, accountUtils: AccountUtils = ComposeAppGraph.accountUtils) {
    val scope = rememberCoroutineScope()

    DayScreen(
        modifier = modifier,
        onDisconnect = {
            scope.launch {
                accountUtils.removeUser(accountUtils.currentUserIdFlow.first() ?: return@launch)
            }
        },
    )
}

@Composable
private fun DayScreen(onDisconnect: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(topBar = { Text("HomeScreen") }, modifier = modifier.windowInsetsPadding(WindowInsets.statusBars)) { paddingValues ->
        Column {
            Text("DayScreenContent", modifier = Modifier.padding(paddingValues))
            Text("User: ${LocalUser.current?.displayName}", modifier = Modifier.padding(paddingValues))
            Button(onClick = onDisconnect) { Text("Disconnect") }
        }
    }
}
