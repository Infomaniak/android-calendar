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
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.calendar.component.ExpandableCalendar
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.planning.Planning
import com.infomaniak.calendar.ui.component.topAppBar.CalendarTopAppBar
import com.infomaniak.calendar.ui.navigation.state.scrollableToolbar
import com.infomaniak.calendar.ui.previewparameter.EventsByWeekAndDayPreviewParameter
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.state.VisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlin.time.Clock

@Composable
fun PlanningScreen(
    goToEventCreation: () -> Unit,
    goToEventDetail: (EventId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlanningViewModel = viewModel(),
) {
    val planningUiState: PlanningUiState by viewModel.planningUiState.collectAsStateWithLifecycle()
    val isLoadingEvents by viewModel.isLoadingEvents.collectAsStateWithLifecycle(initialValue = false)

    PlanningScreen(
        goToEventCreation = goToEventCreation,
        goToEventDetail = goToEventDetail,
        planningUiState = { planningUiState },
        isLoadingEvents = { isLoadingEvents },
        modifier = modifier,
    )
}

@Composable
private fun PlanningScreen(
    goToEventCreation: () -> Unit,
    goToEventDetail: (EventId) -> Unit,
    planningUiState: () -> PlanningUiState,
    isLoadingEvents: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    var topBarHeight by remember { mutableStateOf(0.dp) }

    var isCalendarExpanded by rememberSaveable { mutableStateOf(false) }
    val visibleDayState = LocalVisibleDayState.current

    Scaffold(
        modifier = modifier,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { scaffoldContentPadding ->
        val contentPadding = scaffoldContentPadding + PaddingValues(top = topBarHeight)

        Box(modifier = Modifier.fillMaxSize()) {
            when (val planningUi = planningUiState()) {
                is PlanningUiState.Success -> {
                    SuccessPlanning(
                        events = planningUi.eventsByWeekAndDay,
                        contentPadding = contentPadding + PaddingValues(Margin.Medium),
                        goToEventCreation = goToEventCreation,
                        goToEventDetail = goToEventDetail,
                        modifier = Modifier.hazeSource(hazeState),
                    )
                }
                is PlanningUiState.Loading -> {
                    LoadingPlanning(modifier = Modifier.padding(contentPadding))
                }
            }

            CalendarTopAppBar(
                isLoadingEvents = isLoadingEvents,
                hazeState = hazeState,
                onToggleCalendar = { isCalendarExpanded = !isCalendarExpanded },
                calendar = {
                    if (visibleDayState != null) {
                        ExpandableCalendar(
                            isExpanded = { isCalendarExpanded },
                            selectedDate = { visibleDayState.visibleDate },
                            onDayClick = { visibleDayState.jumpTo(it) },
                            weekNumbering = WeekNumbering.ISO_8601, //TODO[weekNumbering]: Use week numbering from LocalSettings
                        )
                    }
                },
                isCalendarExpanded = { isCalendarExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } },
            )
        }
    }
}

@Composable
private fun SuccessPlanning(
    events: () -> EventsByWeekAndDay,
    contentPadding: PaddingValues,
    goToEventCreation: () -> Unit,
    goToEventDetail: (EventId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleDayState = LocalVisibleDayState.current ?: return
    val lazyListState = rememberLazyListState(events().indexOf(visibleDayState.visibleDate))

    ProcessJumpRequests(lazyListState, visibleDayState, events)
    ReportVisibleDate(lazyListState, onVisibleDateChanged = { visibleDayState.onVisibleDateChanged(it) })

    Planning(
        lazyListState = lazyListState,
        weekEvents = events,
        modifier = modifier
            .scrollableToolbar()
            .fillMaxSize(),
        contentPadding = contentPadding,
        goToEventCreation = goToEventCreation,
        goToEventDetail = { goToEventDetail(EventId(it)) },
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
                goToEventCreation = {},
                goToEventDetail = {},
                isLoadingEvents = { false },
            )
        }
    }
}
