package com.infomaniak.calendar.ui.screen.agenda

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(goToSubDestination: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(topBar = { TopAppBar(title = { Text("AgendaScreen") }) }, modifier = modifier) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("AgendaScreenContent")
            Button(onClick = { goToSubDestination() }) { Text("goToSubDestination") }
        }
    }
}

@Preview
@Composable
private fun AgendaScreenPreview() {
    AgendaScreen(goToSubDestination = {})
}
