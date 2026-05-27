package com.infomaniak.calendar.ui.screen.week

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(modifier: Modifier = Modifier) {
    Scaffold(topBar = { TopAppBar(title = { Text("WeekScreen") }, modifier = modifier) }) { paddingValues ->
        Text("WeekScreenContent", modifier = Modifier.padding(paddingValues))
    }
}
