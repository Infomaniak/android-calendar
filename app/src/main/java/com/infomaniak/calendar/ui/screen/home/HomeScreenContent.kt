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
package com.infomaniak.calendar.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.calendar.ui.screen.home.HomeUiState.Companion.HomeUiStatePreviewProvider
import com.infomaniak.calendar.ui.screen.home.composable.Loading
import com.infomaniak.calendar.ui.screen.home.composable.Success

@Composable
fun HomeScreenContent(state: HomeUiState, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { Text("HomeScreen") },
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            when (state) {
                HomeUiState.Loading -> Loading()
                is HomeUiState.Success -> Success(state)
            }
        }
    }
}

@Composable
@Preview
private fun HomeScreenContentPreview(
    @PreviewParameter(HomeUiStatePreviewProvider::class) state: HomeUiState
) {
    HomeScreenContent(state = state)
}
