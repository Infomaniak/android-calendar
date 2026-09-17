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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.infomaniak.calendar.components.calendar.component.ExpandableCalendar
import com.infomaniak.calendar.components.calendar.component.collapseCalendarOnScroll
import com.infomaniak.calendar.components.calendar.component.rememberCalendarExpansionState
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.state.VisibleDayState
import com.infomaniak.calendar.components.foundation.state.rememberVisibleDayState
import com.infomaniak.calendar.components.planning.Planning
import com.infomaniak.calendar.components.planning.PlanningRow
import com.infomaniak.calendar.components.planning.preview.PlanningRowPreviewParameter
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

@Composable
fun PlanningScreen(
    goToEventCreation: (LocalDate) -> Unit,
    goToEventDetail: (occurrenceId: OccurrenceId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlanningViewModel = viewModel(),
) {
    val planningRows = viewModel.planningRows.collectAsLazyPagingItems()
    val isLoadingEvents by viewModel.isLoadingEvents.collectAsStateWithLifecycle(initialValue = false)
    val eventsDots by viewModel.eventDots.collectAsStateWithLifecycle(initialValue = emptyMap())
    val visibleDayState = LocalVisibleDayState.current ?: return

    PlanningScreen(
        goToEventCreation = goToEventCreation,
        goToEventDetail = goToEventDetail,
        planningRows = planningRows,
        onJumpTo = viewModel::jumpTo,
        onVisibleDateChanged = viewModel::onVisibleDateChanged,
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
    planningRows: LazyPagingItems<PlanningRow>,
    onJumpTo: (LocalDate) -> Boolean,
    onVisibleDateChanged: (LocalDate) -> Unit,
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
        // Keep the planning mounted once it has shown content, so a far jump's refresh (itemCount
        // momentarily 0) doesn't tear down the jump/scroll handling — only the very first load shows a spinner.
        var hasLoadedOnce by rememberSaveable { mutableStateOf(false) }
        if (planningRows.itemCount > 0) hasLoadedOnce = true

        if (hasLoadedOnce) {
            SuccessPlanning(
                planningRows = planningRows,
                onJumpTo = onJumpTo,
                onVisibleDateChanged = onVisibleDateChanged,
                contentPadding = contentPadding + PaddingValues(Margin.Medium),
                goToEventCreation = goToEventCreation,
                goToEventDetail = goToEventDetail,
                visibleDayState = visibleDayState,
                modifier = Modifier
                    .hazeSource(hazeState)
                    .collapseCalendarOnScroll(calendarExpansionState),
            )
        } else {
            ScreenLoader(modifier = Modifier.padding(contentPadding))
        }
    }
}

@Composable
private fun SuccessPlanning(
    planningRows: LazyPagingItems<PlanningRow>,
    onJumpTo: (LocalDate) -> Boolean,
    onVisibleDateChanged: (LocalDate) -> Unit,
    contentPadding: PaddingValues,
    goToEventCreation: (LocalDate) -> Unit,
    goToEventDetail: (occurrenceId: OccurrenceId) -> Unit,
    visibleDayState: VisibleDayState,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()

    AlignPlanningToDate(lazyListState, planningRows, visibleDayState, onJumpTo)
    ReportVisibleDate(
        lazyListState = lazyListState,
        onVisibleDateChanged = {
            onVisibleDateChanged(it)
            visibleDayState.updateVisibleDate(it)
        },
    )

    Planning(
        lazyListState = lazyListState,
        rows = planningRows,
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
private fun Preview(@PreviewParameter(PlanningRowPreviewParameter::class) rows: List<PlanningRow>) {
    CalendarThemeForPreview {
        val visibleDayState = rememberVisibleDayState()
        val planningRows = flowOf(PagingData.from(rows)).collectAsLazyPagingItems()

        CompositionLocalProvider(LocalVisibleDayState provides visibleDayState) {
            PlanningScreen(
                planningRows = planningRows,
                onJumpTo = { false },
                onVisibleDateChanged = {},
                goToEventCreation = {},
                goToEventDetail = {},
                isLoadingEvents = { false },
                eventsDots = { emptyMap() },
                visibleDayState = visibleDayState,
            )
        }
    }
}
