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
package com.infomaniak.calendar.ui.screen.planning

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.planning.Planning
import com.infomaniak.calendar.ui.component.drawer.DrawerIconButton
import com.infomaniak.calendar.ui.navigation.state.scrollableToolbar
import com.infomaniak.calendar.ui.previewparameter.EventsByWeekAndDayPreviewParameter
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin

@Composable
fun PlanningScreen(goToEventCreation: () -> Unit, modifier: Modifier = Modifier, viewModel: PlanningViewModel = viewModel()) {
    val planningUiState: PlanningUiState by viewModel.planningUiState.collectAsStateWithLifecycle()
    PlanningScreen(goToEventCreation = goToEventCreation, planningUiState = { planningUiState }, modifier = modifier)
}

@Composable
private fun PlanningScreen(goToEventCreation: () -> Unit, planningUiState: () -> PlanningUiState, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.planningTitle)) },
                navigationIcon = { DrawerIconButton() },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        when (val planningUi = planningUiState()) {
            is PlanningUiState.Success -> SuccessPlanning(goToEventCreation, planningUi, paddingValues)
            is PlanningUiState.Loading -> LoadingPlanning(modifier = Modifier.padding(paddingValues))
        }
    }
}

@Composable
private fun SuccessPlanning(
    goToEventCreation: () -> Unit,
    planningUi: PlanningUiState.Success,
    contentPadding: PaddingValues,
) {
    Column {
        Planning(
            weekEvents = { planningUi.eventsByWeekAndDay },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Margin.Medium)
                .scrollableToolbar()
                .fillMaxWidth(),
            contentPadding = contentPadding,
            goToEventCreation = goToEventCreation,
        )
    }
}

@Composable
private fun LoadingPlanning(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(EventsByWeekAndDayPreviewParameter::class) weekEvents: EventsByWeekAndDay) {
    CalendarThemeForPreview {
        PlanningScreen(goToEventCreation = { }, planningUiState = { PlanningUiState.Success(weekEvents) })
    }
}
