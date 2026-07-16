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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.planning.Planning
import com.infomaniak.calendar.ui.component.topAppBar.CalendarTopAppBar
import com.infomaniak.calendar.ui.navigation.state.scrollableToolbar
import com.infomaniak.calendar.ui.previewparameter.EventsByWeekAndDayPreviewParameter
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.state.VisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.margin.Margin
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Composable
fun PlanningScreen(goToEventCreation: () -> Unit, modifier: Modifier = Modifier, viewModel: PlanningViewModel = viewModel()) {
    val planningUiState: PlanningUiState by viewModel.planningUiState.collectAsStateWithLifecycle()
    val nextEvents: List<EventUi.Normal> by viewModel.nextEvents.collectAsStateWithLifecycle()
    val isLoadingEvents by viewModel.isLoadingEvents.collectAsStateWithLifecycle(initialValue = false)

    PlanningScreen(
        goToEventCreation = goToEventCreation,
        planningUiState = { planningUiState },
        nextEvents = { nextEvents },
        isLoadingEvents = { isLoadingEvents },
        modifier = modifier,
    )
}

@Composable
private fun PlanningScreen(
    goToEventCreation: () -> Unit,
    planningUiState: () -> PlanningUiState,
    nextEvents: () -> List<EventUi.Normal>,
    isLoadingEvents: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val topBarTint = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current
    var topBarHeight by remember { mutableStateOf(0.dp) }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val planningUi = planningUiState()) {
                is PlanningUiState.Success -> {
                    SuccessPlanning(
                        events = planningUi.eventsByWeekAndDay,
                        nextEvents = nextEvents,
                        contentPadding = PaddingValues(top = topBarHeight, bottom = bottomInset) + PaddingValues(Margin.Medium),
                        goToEventCreation = goToEventCreation,
                        hazeState = hazeState,
                    )
                }
                is PlanningUiState.Loading -> {
                    LoadingPlanning(modifier = Modifier.fillMaxSize())
                }
            }

            CalendarTopAppBar(
                isLoadingEvents = isLoadingEvents,
                containerColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } }
                    .hazeEffect(state = hazeState) {
                        blurEffect {
                            blurRadius = 24.dp
                            colorEffects = listOf(HazeColorEffect.tint(topBarTint.copy(alpha = 0.4f)))
                        }
                    },
            )
        }
    }
}

@Composable
private fun SuccessPlanning(
    events: () -> EventsByWeekAndDay,
    nextEvents: () -> List<EventUi.Normal>,
    contentPadding: PaddingValues,
    goToEventCreation: () -> Unit,
    hazeState: HazeState,
) {
    val visibleDayState = LocalVisibleDayState.current ?: return
    val lazyListState = rememberLazyListState(events().indexOf(visibleDayState.visibleDate))

    ProcessJumpRequests(lazyListState, visibleDayState, events)
    ReportVisibleDate(lazyListState, onVisibleDateChanged = visibleDayState::onVisibleDateChanged)

    Planning(
        lazyListState = lazyListState,
        weekEvents = events,
        nextEvents = nextEvents,
        hazeState = hazeState,
        modifier = Modifier
            .scrollableToolbar()
            .fillMaxSize(),
        contentPadding = contentPadding,
        goToEventCreation = goToEventCreation,
    )
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
        val visibleDate = remember { mutableStateOf(Clock.today()) }

        CompositionLocalProvider(LocalVisibleDayState provides VisibleDayState(visibleDate)) {
            PlanningScreen(
                planningUiState = { PlanningUiState.Success({ weekEvents }) },
                nextEvents = { weekEvents.findNextEvents(Clock.System.now()) },
                goToEventCreation = {},
                isLoadingEvents = { false },
            )
        }
    }
}
