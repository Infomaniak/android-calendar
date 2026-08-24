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
package com.infomaniak.calendar.ui.screen.day

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.calendar.component.ExpandableCalendar
import com.infomaniak.calendar.components.day.DayPager
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.ui.component.topAppBar.CalendarTopAppBar
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.state.VisibleDayState
import com.infomaniak.calendar.ui.state.rememberVisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.utils.today
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filterNot
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Clock

@Composable
fun DayScreen(modifier: Modifier = Modifier, dayViewModel: DayViewModel = viewModel()) {
    val dayUiState by dayViewModel.dayUiState.collectAsStateWithLifecycle()
    val isLoadingEvents by dayViewModel.isLoadingEvents.collectAsStateWithLifecycle(initialValue = false)
    val visibleDayState = LocalVisibleDayState.current ?: return
    // The timeline scrolls to its opening hour as soon as it is measured, and counts that scroll in
    // hour heights: it is built once the stored height is known, or it would open hours off.
    val storedHourHeight by dayViewModel.hourHeight.collectAsStateWithLifecycle(initialValue = null)
    val timelineState = rememberDayTimelineState(initialHourHeight = storedHourHeight ?: return)

    SaveHourHeight(timelineState, onHourHeightChanged = dayViewModel::saveHourHeight)
    ApplyJumpRequests(visibleDayState)

    DayScreen(
        modifier = modifier,
        dayUiState = { dayUiState },
        visibleDayState = visibleDayState,
        timelineState = timelineState,
        dateRange = dayViewModel.dateRange,
        isLoadingEvents = { isLoadingEvents },
    )
}

/**
 * Turns a jump request, such as tapping a day in the calendar, into a change of visible date. The
 * pager then animates to it on its own, since it follows the visible date.
 */
@Composable
private fun ApplyJumpRequests(visibleDayState: VisibleDayState) {
    LaunchedEffect(visibleDayState) {
        for (date in visibleDayState.scrollCommand) visibleDayState.onVisibleDateChanged(date)
    }
}

/**
 * Stores the zoom level the user leaves the day view on.
 *
 * A pinch changes the height on every frame, so the height is stored once the gesture ends: one
 * write per pinch, and nothing left waiting on a timer that leaving the screen would cancel.
 */
@Composable
private fun SaveHourHeight(timelineState: DayTimelineState, onHourHeightChanged: suspend (Dp) -> Unit) {
    LaunchedEffect(timelineState) {
        snapshotFlow { timelineState.isPinching }
            .dropWhile { isPinching -> !isPinching }
            .filterNot { isPinching -> isPinching }
            .collect { onHourHeightChanged(timelineState.hourHeight) }
    }
}

@Composable
private fun DayScreen(
    dayUiState: () -> DayUiState,
    isLoadingEvents: () -> Boolean,
    visibleDayState: VisibleDayState,
    timelineState: DayTimelineState,
    dateRange: ClosedRange<LocalDate>,
    modifier: Modifier = Modifier,
) {
    var isCalendarExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CalendarTopAppBar(
                isLoadingEvents = isLoadingEvents,
                onToggleCalendar = { isCalendarExpanded = !isCalendarExpanded },
                isCalendarExpanded = { isCalendarExpanded },
                hazeState = null,
                calendar = {
                    ExpandableCalendar(
                        isExpanded = { isCalendarExpanded },
                        selectedDate = { visibleDayState.visibleDate },
                        onDayClick = { visibleDayState.jumpTo(it) },
                        weekNumbering = WeekNumbering.ISO_8601, //TODO[weekNumbering]: Use week numbering from LocalSettings
                        eventsDots = { emptyMap() },
                    )
                },
            )
        },
        // The bottom insets stay out so the timeline can run under the navigation bar; it makes
        // room for it in its own scrolled content instead.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        modifier = modifier,
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = dayUiState()) {
                is DayUiState.Loading -> LoadingDay()
                is DayUiState.Success -> SuccessDay(visibleDayState, timelineState, dateRange, state.eventsByDate)
            }
        }
    }
}

@Composable
private fun SuccessDay(
    visibleDayState: VisibleDayState,
    timelineState: DayTimelineState,
    dateRange: ClosedRange<LocalDate>,
    eventsByDate: () -> DayEventsByDate,
    modifier: Modifier = Modifier,
) {
    DayPager(
        dateRange = dateRange,
        selectedDate = { visibleDayState.visibleDate },
        eventsOf = { eventsByDate()[it] ?: DayEvents.Empty },
        state = timelineState,
        weekNumbering = WeekNumbering.ISO_8601, //TODO[weekNumbering]: Use week numbering from LocalSettings
        onVisibleDateChanged = { visibleDayState.onVisibleDateChanged(it) },
        onEventClick = {}, //TODO[eventDetail]: Open the event detail screen
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun LoadingDay(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Preview
@Composable
private fun DayScreenPreview() {
    CalendarThemeForPreview {
        val visibleDate = remember { mutableStateOf(Clock.today()) }

        CompositionLocalProvider(LocalVisibleDayState provides VisibleDayState(visibleDate)) {
            DayScreen(
                dayUiState = { DayUiState.Success({ emptyMap() }) },
                isLoadingEvents = { false },
                visibleDayState = rememberVisibleDayState(visibleDate),
                timelineState = rememberDayTimelineState(),
                dateRange = visibleDate.value.minus(1, DateTimeUnit.DAY)..visibleDate.value.plus(1, DateTimeUnit.DAY),
            )
        }
    }
}
