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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.calendar.component.ExpandableCalendar
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.planning.Planning
import com.infomaniak.calendar.components.planning.PlanningRow
import com.infomaniak.calendar.components.planning.preview.PlanningRowPreviewParameter
import com.infomaniak.calendar.ui.component.topAppBar.CalendarTopAppBar
import com.infomaniak.calendar.ui.model.occurrenceId
import com.infomaniak.calendar.ui.navigation.state.scrollableToolbar
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.state.VisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.R as RCore
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

@Composable
fun PlanningScreen(
    goToEventCreation: () -> Unit,
    goToEventDetail: (occurrenceId: OccurrenceId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlanningViewModel = viewModel(),
) {
    val planningRows = viewModel.planningRows.collectAsLazyPagingItems()
    val isLoadingEvents by viewModel.isLoadingEvents.collectAsStateWithLifecycle(initialValue = false)
    val eventsDots by viewModel.eventDots.collectAsStateWithLifecycle(initialValue = emptyMap())

    PlanningScreen(
        callbacks = PlanningScreenCallbacks(
            goToEventCreation = goToEventCreation,
            goToEventDetail = goToEventDetail,
            onJumpTo = viewModel::jumpTo,
            onNavigationFinished = viewModel::onNavigationFinished,
            onVisibleDateChanged = viewModel::onVisibleDateChanged,
        ),
        planningRows = planningRows,
        isLoadingEvents = { isLoadingEvents },
        eventsDots = { eventsDots },
        modifier = modifier,
    )
}

private data class PlanningScreenCallbacks(
    val goToEventCreation: () -> Unit,
    val goToEventDetail: (OccurrenceId) -> Unit,
    val onJumpTo: (LocalDate) -> Long,
    val onNavigationFinished: (Long) -> Unit,
    val onVisibleDateChanged: (LocalDate) -> Unit,
)

@Composable
private fun PlanningScreen(
    callbacks: PlanningScreenCallbacks,
    planningRows: LazyPagingItems<PlanningRow>,
    isLoadingEvents: () -> Boolean,
    eventsDots: () -> Map<LocalDate, List<EventColorsUi>>,
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
            // Keep the planning mounted once it has shown content, so a far jump's refresh (itemCount
            // momentarily 0) doesn't tear down the jump/scroll handling — only the very first load shows a spinner.
            var hasLoadedOnce by rememberSaveable { mutableStateOf(false) }
            if (planningRows.itemCount > 0) hasLoadedOnce = true
            val hasLoadError = planningRows.hasLoadError()

            if (hasLoadedOnce) {
                SuccessPlanning(
                    planningRows = planningRows,
                    callbacks = callbacks,
                    contentPadding = contentPadding + PaddingValues(Margin.Medium),
                    modifier = Modifier.hazeSource(hazeState),
                )
                if (hasLoadError) {
                    PagingLoadError(
                        onRetry = planningRows::retry,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(contentPadding)
                            .padding(Margin.Medium),
                    )
                }
            } else if (hasLoadError) {
                InitialPlanningError(onRetry = planningRows::retry, modifier = Modifier.padding(contentPadding))
            } else {
                LoadingPlanning(modifier = Modifier.padding(contentPadding))
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
                            eventsDots = eventsDots,
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
    planningRows: LazyPagingItems<PlanningRow>,
    callbacks: PlanningScreenCallbacks,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val visibleDayState = LocalVisibleDayState.current ?: return
    val lazyListState = rememberLazyListState()
    var initialAlignmentCompleted by remember { mutableStateOf(false) }

    AlignPlanningToDate(
        lazyListState = lazyListState,
        planningRows = planningRows,
        visibleDayState = visibleDayState,
        onJumpTo = callbacks.onJumpTo,
        onNavigationFinished = callbacks.onNavigationFinished,
        onInitialAlignmentCompleted = { initialAlignmentCompleted = true },
    )
    ReportVisibleDate(
        lazyListState = lazyListState,
        onVisibleDateChanged = {
            callbacks.onVisibleDateChanged(it)
            visibleDayState.onVisibleDateChanged(it)
        },
    )

    Box(modifier = modifier) {
        Planning(
            lazyListState = lazyListState,
            rows = planningRows,
            modifier = Modifier
                .scrollableToolbar()
                .fillMaxSize()
                .alpha(if (initialAlignmentCompleted) 1f else 0f),
            contentPadding = contentPadding,
            goToEventCreation = callbacks.goToEventCreation,
            onEventClick = { callbacks.goToEventDetail(it.occurrenceId) },
        )
        if (!initialAlignmentCompleted) LoadingPlanning()
    }
}

@Composable
private fun LoadingPlanning(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun InitialPlanningError(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Margin.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Margin.Medium, Alignment.CenterVertically),
    ) {
        Text(text = stringResource(R.string.syncEventsError), textAlign = TextAlign.Center)
        Button(onClick = onRetry) { Text(stringResource(RCore.string.buttonRetry)) }
    }
}

@Composable
private fun PagingLoadError(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier.padding(Margin.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Margin.Small),
        ) {
            Text(text = stringResource(R.string.syncEventsError), textAlign = TextAlign.Center)
            Button(onClick = onRetry) { Text(stringResource(RCore.string.buttonRetry)) }
        }
    }
}

private fun LazyPagingItems<PlanningRow>.hasLoadError(): Boolean {
    val loadStates = loadState
    return loadStates.refresh is LoadState.Error ||
        loadStates.prepend is LoadState.Error ||
        loadStates.append is LoadState.Error
}

@Preview
@Composable
private fun Preview(@PreviewParameter(PlanningRowPreviewParameter::class) rows: List<PlanningRow>) {
    CalendarThemeForPreview {
        val visibleDate = remember { mutableStateOf(Clock.today()) }
        val planningRows = flowOf(PagingData.from(rows)).collectAsLazyPagingItems()

        CompositionLocalProvider(LocalVisibleDayState provides VisibleDayState(visibleDate)) {
            PlanningScreen(
                callbacks = PlanningScreenCallbacks(
                    goToEventCreation = {},
                    goToEventDetail = {},
                    onJumpTo = { 0L },
                    onNavigationFinished = {},
                    onVisibleDateChanged = {},
                ),
                planningRows = planningRows,
                isLoadingEvents = { false },
                eventsDots = { emptyMap() },
            )
        }
    }
}
