package com.infomaniak.calendar.ui.screen.subDestinationTest

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubDestinationScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("SubDestinationScreen") }, modifier = modifier) },
    ) { paddingValues ->
        Text("SubDestinationScreenContent", modifier = Modifier.padding(paddingValues))
    }
}
