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
package com.infomaniak.calendar.ui.screen.eventFormTest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.infomaniak.calendar.di.ComposeAppGraph
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.screen.eventFormTest.composable.EventFormFields

fun EntryProviderScope<NavKey>.eventFormTest(
    onNavigateBack: () -> Unit,
) = entry<NavDestination.EventFormTest> { destination ->
    val factory = ComposeAppGraph.eventFormTestViewModelFactory
    val viewModel = viewModel(key = destination.eventId?.url ?: "create") {
        factory.create(destination.eventId)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EventFormUiEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    EventFormTestScreenContent(
        isEdition = destination.eventId != null,
        state = state,
        processAction = viewModel::processAction,
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormTestScreenContent(
    isEdition: Boolean,
    state: EventFormUiState,
    processAction: (EventFormAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSaving = (state as? EventFormUiState.Editing)?.isSaving == true
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdition) "Modifier l'événement" else "Nouvel événement") },
                navigationIcon = {
                    IconButton(onClick = { processAction(EventFormAction.OnClickBack) }) { Text("←") }
                },
                actions = {
                    TextButton(
                        onClick = { processAction(EventFormAction.OnClickSave) },
                        enabled = state is EventFormUiState.Editing && !isSaving,
                    ) { Text("Enregistrer") }
                },
            )
        },
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            when (state) {
                EventFormUiState.Loading -> Loader()
                is EventFormUiState.Editing -> EventFormFields(state = state, processAction = processAction)
                is EventFormUiState.Error -> Text(text = state.message, modifier = Modifier.padding(16.dp))
            }
        }
    }
}
@Composable
private fun Loader() = Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator()
}


