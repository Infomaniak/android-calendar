package com.infomaniak.calendar.ui.screen.month

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.ui.screen.day.DayScreen

@Composable
fun MonthScreen(modifier: Modifier = Modifier, monthViewModel: MonthViewModel = viewModel()) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("MonthScreen") }) },
        modifier = modifier,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
        ) {
            items(50) { index ->
                Text(
                    text = "Événement du mois n°$index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun MonthScreenPreview() {
    MonthScreen()
}
