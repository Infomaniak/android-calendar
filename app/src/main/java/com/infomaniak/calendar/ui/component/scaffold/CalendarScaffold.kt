package com.infomaniak.calendar.ui.component.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.ui.component.CalendarFab

@Composable
fun CalendarScaffoldWithFab(
    onFabAction: (() -> Unit),
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = { CalendarFab(onClick = onFabAction) },
        topBar = topBar,
        content = content,
    )
}

@Preview
@Composable
private fun CalendarScaffoldPreview() {
    CalendarScaffoldWithFab(onFabAction = {}, content = {})
}
