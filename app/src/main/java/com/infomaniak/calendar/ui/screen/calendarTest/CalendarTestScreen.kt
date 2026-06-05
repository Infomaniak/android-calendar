/*
 * Infomaniak Calendar - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.calendar.ui.screen.calendarTest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.screen.calendarTest.composable.Error
import com.infomaniak.calendar.ui.screen.calendarTest.composable.Loaded
import com.infomaniak.calendar.ui.screen.calendarTest.composable.Loading
import com.infomaniak.calendar.ui.screen.calendarTest.previewParameter.CalendarTestUiStatePreviewProvider

fun EntryProviderScope<NavKey>.calendarTest() = entry<NavDestination.CalendarTest> {
    val viewModel = viewModel<CalendarTestViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarTestScreenContent(state = state)
}

@Composable
fun CalendarTestScreenContent(state: CalendarTestUiState, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { Text("CalendarTestScreen") },
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            when (state) {
                CalendarTestUiState.Loading -> Loading()
                is CalendarTestUiState.Loaded -> Loaded(state)
                is CalendarTestUiState.Error -> Error(state)
            }
        }
    }
}

@Composable
@Preview
private fun CalendarTestScreenContentPreview(
    @PreviewParameter(CalendarTestUiStatePreviewProvider::class) state: CalendarTestUiState,
) {
    CalendarTestScreenContent(state = state)
}
