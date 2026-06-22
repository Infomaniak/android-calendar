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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.di.ComposeAppGraph
import com.infomaniak.calendar.ui.LocalUser
import com.infomaniak.calendar.ui.component.drawer.CalendarDrawerIcon
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.utils.account.AccountUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun DayScreen(
    goToTestScreen: () -> Unit,
    modifier: Modifier = Modifier,
    accountUtils: AccountUtils = ComposeAppGraph.accountUtils,
) {
    val scope = rememberCoroutineScope()

    DayScreen(
        modifier = modifier,
        onDisconnect = {
            scope.launch {
                accountUtils.removeUser(accountUtils.currentUserIdFlow.first() ?: return@launch)
            }
        },
        goToTestScreen = goToTestScreen,
    )
}

@Composable
private fun DayScreen(
    onDisconnect: () -> Unit,
    goToTestScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.dayTitle)) }, navigationIcon = { CalendarDrawerIcon() }) },
        modifier = modifier,
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("DayScreenContent")
            Text("Current user: ${LocalUser.current?.displayName}")
            Button(onClick = onDisconnect) { Text("Disconnect") }
            Button(onClick = goToTestScreen) { Text("Test Calendar") }
        }
    }
}

@Preview
@Composable
private fun DayScreenPreview() {
    CalendarThemeForPreview {
        DayScreen(goToTestScreen = { }, onDisconnect = { })
    }
}
