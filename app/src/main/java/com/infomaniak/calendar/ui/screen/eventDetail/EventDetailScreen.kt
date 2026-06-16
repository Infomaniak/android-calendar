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
package com.infomaniak.calendar.ui.screen.eventDetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.infomaniak.calendar.di.ComposeAppGraph
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.screen.eventDetail.composable.Content
import com.infomaniak.calendar.ui.screen.eventDetail.composable.Error
import com.infomaniak.calendar.ui.screen.eventDetail.composable.EventScreenHeader
import com.infomaniak.calendar.ui.screen.eventDetail.composable.Loading
import com.infomaniak.calendar.ui.screen.eventDetail.previewParameter.EventDetailUiStatePreviewProvider
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview

fun EntryProviderScope<NavKey>.eventDetail(
    onNavigateBack: () -> Unit,
) = entry<NavDestination.EventDetail> { destination ->
    val factory = ComposeAppGraph.eventDetailViewModelFactory
    val viewModel = viewModel(key = destination.eventId.url) {
        factory.create(destination.eventId)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EventDetailUiEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    EventDetailScreenContent(
        state = state,
        processAction = viewModel::processAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreenContent(
    state: EventDetailUiState,
    modifier: Modifier = Modifier,
    processAction: (EventDetailAction) -> Unit = {},
) {
    val title = (state as? EventDetailUiState.Loaded)?.title ?: "Détail"
    Scaffold(
        topBar = { EventScreenHeader(title, processAction) },
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            when (state) {
                EventDetailUiState.Loading -> Loading()
                is EventDetailUiState.Loaded -> Content(state = state, processAction = processAction)
                is EventDetailUiState.Error -> Error(state = state)
            }
        }
    }
}

@Composable
@Preview
private fun EventDetailScreenContentPreview(
    @PreviewParameter(EventDetailUiStatePreviewProvider::class) state: EventDetailUiState,
) = CalendarThemeForPreview {
    EventDetailScreenContent(state = state)
}
