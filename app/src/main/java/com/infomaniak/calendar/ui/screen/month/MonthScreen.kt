package com.infomaniak.calendar.ui.screen.month

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.ui.navigation.LocalGlobalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthScreen(modifier: Modifier = Modifier) {
    val globalPadding = LocalGlobalPadding.current
    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("MonthScreen") }) },
        modifier = modifier,
    ) { localPadding ->
        val combinedPadding = PaddingValues(
            top = localPadding.calculateTopPadding(),
            bottom = globalPadding.calculateBottomPadding(),
            start = localPadding.calculateStartPadding(layoutDirection),
            end = localPadding.calculateEndPadding(layoutDirection)
        )

        PaddingValues()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = combinedPadding,
        ) {
            items(50) { index ->
                Text(
                    text = "Événement du mois n°$index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}
