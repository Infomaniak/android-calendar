package com.infomaniak.calendar.ui.screen.month

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.infomaniak.calendar.ui.component.scaffold.CalendarScaffoldWithFab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthScreen(modifier: Modifier = Modifier) {
    CalendarScaffoldWithFab(
        topBar = { TopAppBar(title = { Text("MonthScreen") }) },
        onFabAction = {},
        modifier = modifier,
    ) { paddingValues ->
        Text("MonthScreenContent", modifier = Modifier.padding(paddingValues))
    }
}
