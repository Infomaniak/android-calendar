package com.infomaniak.calendar.ui.screen.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Scaffold(topBar = { Text("HomeScreen") }, modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) { paddingValues ->
        Text("HomeScreenContent", modifier = modifier.padding(paddingValues))
    }
}
