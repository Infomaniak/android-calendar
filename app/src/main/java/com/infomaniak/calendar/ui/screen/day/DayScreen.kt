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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.infomaniak.calendar.di.ComposeAppGraph
import com.infomaniak.calendar.ui.LocalUser
import com.infomaniak.calendar.utils.AccountUtils
import com.infomaniak.core.auth.models.user.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun DayScreen(
    onTestScreen: () -> Unit,
    addAddAccount: () -> Unit,
    modifier: Modifier = Modifier,
    accountUtils: AccountUtils = ComposeAppGraph.accountUtils,
) {
    val scope = rememberCoroutineScope()
    val users = accountUtils.users.collectAsStateWithLifecycle(emptyList())

    DayScreen(
        modifier = modifier,
        onDisconnect = {
            scope.launch {
                accountUtils.removeUser(accountUtils.currentUserIdFlow.first() ?: return@launch)
            }
        },
        users = users.value,
        addAddAccount = addAddAccount,
        onTestScreen = onTestScreen,
    )
}

@Composable
private fun DayScreen(
    onDisconnect: () -> Unit,
    onTestScreen: () -> Unit,
    addAddAccount: () -> Unit,
    modifier: Modifier = Modifier,
    users: List<User> = emptyList(),
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("DayScreen") }) },
        modifier = modifier,
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("DayScreenContent")
            Text("Current user: ${LocalUser.current?.displayName}")
            Text("List users:")
            LazyColumn {
                items(users) {
                    Text(it.email)
                }
            }

            Button(onClick = onDisconnect) { Text("Disconnect") }
            Button(onClick = onTestScreen) { Text("Test Calendar") }
            Button(onClick = addAddAccount) { Text("Add Account") }
        }
    }
}

@Preview
@Composable
private fun DayScreenPreview() {
    DayScreen(onDisconnect = { }, onTestScreen = { }, addAddAccount = { })
}
