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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.calendar.component.ExpandableCalendar
import com.infomaniak.calendar.components.calendar.component.collapseCalendarOnScroll
import com.infomaniak.calendar.components.calendar.component.rememberCalendarExpansionState
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.state.VisibleDayState
import com.infomaniak.calendar.components.foundation.state.rememberVisibleDayState
import com.infomaniak.calendar.components.planning.Planning
import com.infomaniak.calendar.components.planning.PlanningTimeline
import com.infomaniak.calendar.components.planning.preview.PlanningPreviewParameter
import com.infomaniak.calendar.ui.component.OverlaidTopBarScaffold
import com.infomaniak.calendar.ui.component.ScreenLoader
import com.infomaniak.calendar.ui.component.topAppBar.CalendarTopAppBar
import com.infomaniak.calendar.ui.model.occurrenceId
import com.infomaniak.calendar.ui.navigation.state.scrollableToolbar
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.datetime.LocalDate

@Composable
fun PlanningScreen(
    goToEventCreation: (LocalDate) -> Unit,
    goToEventDetail: (occurrenceId: OccurrenceId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlanningViewModel = viewModel(),
) {
    val planningUiState: PlanningUiState by viewModel.planningUiState.collectAsStateWithLifecycle()
    val isLoadingEvents by viewModel.isLoadingEvents.collectAsStateWithLifecycle(initialValue = false)
    val eventsDots by viewModel.eventDots.collectAsStateWithLifecycle(initialValue = emptyMap())
    val visibleDayState = LocalVisibleDayState.current ?: return

    PlanningScreen(
        goToEventCreation = goToEventCreation,
        goToEventDetail = goToEventDetail,
        planningUiState = { planningUiState },
        isLoadingEvents = { isLoadingEvents },
        eventsDots = { eventsDots },
        visibleDayState = visibleDayState,
        modifier = modifier,
    )
}

@Composable
private fun PlanningScreen(
    goToEventCreation: (LocalDate) -> Unit,
    goToEventDetail: (occurrenceId: OccurrenceId) -> Unit,
    planningUiState: () -> PlanningUiState,
    isLoadingEvents: () -> Boolean,
    eventsDots: () -> Map<LocalDate, List<EventColorsUi>>,
    visibleDayState: VisibleDayState,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val calendarExpansionState = rememberCalendarExpansionState()

    OverlaidTopBarScaffold(
        topBar = {
            CalendarTopAppBar(
                isLoadingEvents = isLoadingEvents,
                hazeState = hazeState,
                onToggleCalendar = calendarExpansionState::toggle,
                calendar = {
                    ExpandableCalendar(
                        visibleDayState = visibleDayState,
                        expansionState = calendarExpansionState,
                        weekNumbering = WeekNumbering.ISO_8601, //TODO[weekNumbering]: Use week numbering from LocalSettings
                        eventsDots = eventsDots,
                    )
                },
                calendarExpansionProgress = { calendarExpansionState.progress },
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        when (val planningUi = planningUiState()) {
            is PlanningUiState.Success -> {
                SuccessPlanning(
                    eventsByDate = planningUi.eventsByDate,
                    contentPadding = contentPadding + PaddingValues(Margin.Medium),
                    goToEventCreation = goToEventCreation,
                    goToEventDetail = goToEventDetail,
                    visibleDayState = visibleDayState,
                    modifier = Modifier
                        .hazeSource(hazeState)
                        .collapseCalendarOnScroll(calendarExpansionState),
                )
            }
            is PlanningUiState.Loading -> {
                ScreenLoader(modifier = Modifier.padding(contentPadding))
            }
        }
    }
}

@Composable
private fun SuccessPlanning(
    eventsByDate: Map<LocalDate, List<EventSummaryUi>>,
    contentPadding: PaddingValues,
    goToEventCreation: (LocalDate) -> Unit,
    goToEventDetail: (occurrenceId: OccurrenceId) -> Unit,
    visibleDayState: VisibleDayState,
    modifier: Modifier = Modifier,
) {
    val timeline = remember {
        PlanningTimeline(WeekNumbering.ISO_8601) //TODO[weekNumbering]: Use week numbering from LocalSettings
    }
    val lazyListState = rememberLazyListState(timeline.indexOf(visibleDayState.visibleDate))

    ProcessJumpRequests(lazyListState, visibleDayState, timeline)
    ReportVisibleDate(lazyListState, timeline, onVisibleDateChanged = visibleDayState::updateVisibleDate)

    Planning(
        timeline = timeline,
        eventsOf = eventsByDate::get,
        lazyListState = lazyListState,
        modifier = modifier
            .scrollableToolbar()
            .fillMaxSize(),
        contentPadding = contentPadding,
        goToEventCreation = goToEventCreation,
        onEventClick = { goToEventDetail(it.occurrenceId) },
    )
}

@Preview
@Composable
private fun Preview(@PreviewParameter(PlanningPreviewParameter::class) eventsByDate: Map<LocalDate, List<EventSummaryUi>>) {
    CalendarThemeForPreview {
        val visibleDayState = rememberVisibleDayState(remember { mutableStateOf(eventsByDate.keys.min()) })
        CompositionLocalProvider(LocalVisibleDayState provides visibleDayState) {
            PlanningScreen(
                planningUiState = { PlanningUiState.Success(eventsByDate) },
                goToEventCreation = {},
                goToEventDetail = {},
                isLoadingEvents = { false },
                eventsDots = { emptyMap() },
                visibleDayState = visibleDayState,
            )
        }
    }
}
