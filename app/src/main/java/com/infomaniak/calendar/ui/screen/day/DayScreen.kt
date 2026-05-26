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
