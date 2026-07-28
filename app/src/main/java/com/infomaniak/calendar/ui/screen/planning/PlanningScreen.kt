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
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.infomaniak.calendar.components.calendar.component.ExpandableCalendar
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.planning.Planning
import com.infomaniak.calendar.components.planning.PlanningRow
import com.infomaniak.calendar.components.planning.preview.PlanningRowPreviewParameter
import com.infomaniak.calendar.ui.component.topAppBar.CalendarTopAppBar
import com.infomaniak.calendar.ui.navigation.state.scrollableToolbar
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.state.VisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.margin.Margin
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

@Composable
fun PlanningScreen(goToEventCreation: () -> Unit, modifier: Modifier = Modifier, viewModel: PlanningViewModel = viewModel()) {
    val planningRows = viewModel.planningRows.collectAsLazyPagingItems()
    val isLoadingEvents by viewModel.isLoadingEvents.collectAsStateWithLifecycle(initialValue = false)

    PlanningScreen(
        goToEventCreation = goToEventCreation,
        planningRows = planningRows,
        onJumpTo = viewModel::jumpTo,
        isLoadingEvents = { isLoadingEvents },
        modifier = modifier,
    )
}

@Composable
private fun PlanningScreen(
    goToEventCreation: () -> Unit,
    planningRows: LazyPagingItems<PlanningRow>,
    onJumpTo: (LocalDate) -> Boolean,
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
            // Keep the planning mounted once it has shown content, so a far jump's refresh (itemCount
            // momentarily 0) doesn't tear down the jump/scroll handling — only the very first load shows a spinner.
            var hasLoadedOnce by rememberSaveable { mutableStateOf(false) }
            if (planningRows.itemCount > 0) hasLoadedOnce = true

            if (hasLoadedOnce) {
                SuccessPlanning(
                    planningRows = planningRows,
                    onJumpTo = onJumpTo,
                    contentPadding = contentPadding + PaddingValues(Margin.Medium),
                    goToEventCreation = goToEventCreation,
                    modifier = Modifier.hazeSource(hazeState),
                )
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
    onJumpTo: (LocalDate) -> Boolean,
    contentPadding: PaddingValues,
    goToEventCreation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleDayState = LocalVisibleDayState.current ?: return
    val lazyListState = rememberLazyListState()

    AlignPlanningToDate(lazyListState, planningRows, visibleDayState, onJumpTo)
    ReportVisibleDate(lazyListState, onVisibleDateChanged = { visibleDayState.onVisibleDateChanged(it) })

    Planning(
        lazyListState = lazyListState,
        rows = planningRows,
        modifier = modifier
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
private fun Preview(@PreviewParameter(PlanningRowPreviewParameter::class) rows: List<PlanningRow>) {
    CalendarThemeForPreview {
        val visibleDate = remember { mutableStateOf(Clock.today()) }
        val planningRows = flowOf(PagingData.from(rows)).collectAsLazyPagingItems()

        CompositionLocalProvider(LocalVisibleDayState provides VisibleDayState(visibleDate)) {
            PlanningScreen(
                planningRows = planningRows,
                onJumpTo = { false },
                goToEventCreation = {},
                isLoadingEvents = { false },
            )
        }
    }
}
