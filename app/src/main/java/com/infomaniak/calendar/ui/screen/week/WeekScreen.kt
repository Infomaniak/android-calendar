package com.infomaniak.calendar.ui.screen.week

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.ui.navigation.scroll.LocalSnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(modifier: Modifier = Modifier) {
    val snackbarHostState = LocalSnackbarHostState.current

    Scaffold(topBar = { TopAppBar(title = { Text("WeekScreen") }, modifier = modifier) }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = {
                    snackbarHostState?.showSnackbar("Action effectuée avec succès !")
                },
            ) {
                Text("Afficher la Snackbar")
            }
        }
    }
}

@Preview
@Composable
private fun WeekScreenPreview() {
    WeekScreen()
}
