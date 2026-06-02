package com.infomaniak.calendar.ui.screen.planning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(goToSubDestination: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(topBar = { TopAppBar(title = { Text("PlanningScreen") }) }, modifier = modifier) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("PlanningScreenContent")
            Button(onClick = { goToSubDestination() }) { Text("Test") }
        }
    }
}
