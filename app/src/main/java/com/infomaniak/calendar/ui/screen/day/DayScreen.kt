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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.calendar.component.ExpandableCalendar
import com.infomaniak.calendar.components.calendar.component.collapseCalendarOnScroll
import com.infomaniak.calendar.components.calendar.component.rememberCalendarExpansionState
import com.infomaniak.calendar.components.day.DayPager
import com.infomaniak.calendar.components.day.state.TimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.state.VisibleDayState
import com.infomaniak.calendar.components.foundation.state.rememberVisibleDayState
import com.infomaniak.calendar.ui.component.OverlaidTopBarScaffold
import com.infomaniak.calendar.ui.component.topAppBar.CalendarTopAppBar
import com.infomaniak.calendar.ui.effects.ApplyJumpRequests
import com.infomaniak.calendar.ui.model.TimelineUiState
import com.infomaniak.calendar.ui.model.occurrenceId
import com.infomaniak.calendar.ui.modifier.backgroundBlur
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

@Composable
fun DayScreen(
    goToEventDetail: (occurrenceId: OccurrenceId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DayViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoadingEvents by viewModel.isLoadingEvents.collectAsStateWithLifecycle(initialValue = false)
    val eventDots by viewModel.eventDots.collectAsStateWithLifecycle()

    val visibleDayState = LocalVisibleDayState.current ?: return
    // The timeline scrolls to its opening hour as soon as it is measured, and counts that scroll in
    // hour heights: it is built once the stored height is known, or it would open hours off.
    val storedHourHeight by viewModel.hourHeight.collectAsStateWithLifecycle(initialValue = null)
    val timelineState = rememberDayTimelineState(
        initialHourHeight = storedHourHeight ?: return,
        onHourHeightSaveRequest = viewModel::saveHourHeight,
    )

    ApplyJumpRequests(visibleDayState)

    DayScreen(
        goToEventDetail = goToEventDetail,
        uiState = { uiState },
        isLoadingEvents = { isLoadingEvents },
        eventsDots = { eventDots },
        visibleDayState = visibleDayState,
        timelineState = timelineState,
        dateRange = viewModel.dateRange,
        modifier = modifier,
    )
}

@Composable
private fun DayScreen(
    goToEventDetail: (occurrenceId: OccurrenceId) -> Unit,
    uiState: () -> TimelineUiState,
    isLoadingEvents: () -> Boolean,
    eventsDots: () -> Map<LocalDate, List<EventColorsUi>>,
    visibleDayState: VisibleDayState,
    timelineState: TimelineState,
    dateRange: ClosedRange<LocalDate>,
    modifier: Modifier = Modifier,
) {
    val calendarExpansionState = rememberCalendarExpansionState()
    val hazeState = rememberHazeState()

    OverlaidTopBarScaffold(
        topBar = {
            CalendarTopAppBar(
                isLoadingEvents = isLoadingEvents,
                onToggleCalendar = calendarExpansionState::toggle,
                calendarExpansionProgress = { calendarExpansionState.progress },
                hazeState = hazeState,
                calendar = {
                    ExpandableCalendar(
                        visibleDayState = visibleDayState,
                        expansionState = calendarExpansionState,
                        weekNumbering = WeekNumbering.ISO_8601, //TODO[weekNumbering]: Use week numbering from LocalSettings
                        eventsDots = eventsDots,
                    )
                },
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        DayPager(
            dateRange = dateRange,
            selectedDate = { visibleDayState.visibleDate },
            eventsOf = { uiState().eventsOf(it) },
            state = timelineState,
            weekNumbering = WeekNumbering.ISO_8601, //TODO[weekNumbering]: Use week numbering from LocalSettings
            onVisibleDateChanged = { visibleDayState.updateVisibleDate(it) },
            onEventClick = { goToEventDetail(it.occurrenceId) },
            headerModifier = Modifier.backgroundBlur(TopAppBarDefaults.topAppBarColors().containerColor, hazeState),
            timelineModifier = Modifier.hazeSource(hazeState),
            contentPadding = contentPadding,
            // Placed here rather than on each page: the hour grid's vertical scroll reaches it through
            // the pager, which only consumes the horizontal axis.
            modifier = Modifier
                .fillMaxSize()
                .collapseCalendarOnScroll(calendarExpansionState),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        val visibleDayState = rememberVisibleDayState()
        val date = visibleDayState.visibleDate

        CompositionLocalProvider(LocalVisibleDayState provides visibleDayState) {
            DayScreen(
                goToEventDetail = {},
                uiState = { TimelineUiState.Success(emptyMap()) },
                isLoadingEvents = { false },
                eventsDots = { emptyMap() },
                visibleDayState = visibleDayState,
                timelineState = rememberDayTimelineState({}),
                dateRange = date.minus(1, DateTimeUnit.DAY)..date.plus(1, DateTimeUnit.DAY),
            )
        }
    }
}
