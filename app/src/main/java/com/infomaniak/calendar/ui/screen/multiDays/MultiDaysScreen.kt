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
package com.infomaniak.calendar.ui.screen.multiDays

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.calendar.component.ExpandableCalendar
import com.infomaniak.calendar.components.calendar.component.ExpandableCalendarDefaults
import com.infomaniak.calendar.components.calendar.component.collapseCalendarOnScroll
import com.infomaniak.calendar.components.calendar.component.rememberCalendarExpansionState
import com.infomaniak.calendar.components.day.ThreeDaysPager
import com.infomaniak.calendar.components.day.WeekPager
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayColumnsState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.state.VisibleDayState
import com.infomaniak.calendar.components.foundation.state.rememberVisibleDayState
import com.infomaniak.calendar.ui.component.OverlaidTopBarScaffold
import com.infomaniak.calendar.ui.component.topAppBar.CalendarTopAppBar
import com.infomaniak.calendar.ui.effects.ApplyJumpRequests
import com.infomaniak.calendar.ui.effects.SaveHourHeight
import com.infomaniak.calendar.ui.modifier.backgroundBlur
import com.infomaniak.calendar.ui.model.occurrenceId
import com.infomaniak.calendar.ui.screen.day.DayEventsByDate
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.time.Clock

/** The views showing several days side by side, which only differ in how many days they hold and turn. */
enum class MultiDaysView { ThreeDays, Week }

@Composable
fun MultiDaysScreen(
    view: MultiDaysView,
    goToEventDetail: (occurrenceId: OccurrenceId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MultiDaysViewModel = viewModel(),
) {
    val isLoadingEvents by viewModel.isLoadingEvents.collectAsStateWithLifecycle(initialValue = false)
    val eventDots by viewModel.eventDots.collectAsStateWithLifecycle()
    val eventsByDate by viewModel.eventsByDate.collectAsStateWithLifecycle()

    val visibleDayState = LocalVisibleDayState.current ?: return
    // The timeline scrolls to its opening hour as soon as it is measured, and counts that scroll in
    // hour heights: it is built once the stored height is known, or it would open hours off.
    val storedHourHeight by viewModel.hourHeight.collectAsStateWithLifecycle(initialValue = null)
    val timelineState = rememberDayTimelineState(initialHourHeight = storedHourHeight ?: return)

    SaveHourHeight(timelineState, onHourHeightChanged = viewModel::saveHourHeight)
    ApplyJumpRequests(visibleDayState)

    MultiDaysScreen(
        view = view,
        goToEventDetail = goToEventDetail,
        dateRange = viewModel.dateRange,
        eventsByDate = { eventsByDate },
        isLoadingEvents = { isLoadingEvents },
        eventsDots = { eventDots },
        visibleDayState = visibleDayState,
        timelineState = timelineState,
        modifier = modifier,
    )
}

@Composable
private fun MultiDaysScreen(
    view: MultiDaysView,
    goToEventDetail: (occurrenceId: OccurrenceId) -> Unit,
    dateRange: ClosedRange<LocalDate>,
    eventsByDate: () -> DayEventsByDate,
    isLoadingEvents: () -> Boolean,
    eventsDots: () -> Map<LocalDate, List<EventColorsUi>>,
    visibleDayState: VisibleDayState,
    timelineState: DayTimelineState,
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
                        collapsedContent = ExpandableCalendarDefaults.None,
                    )
                },
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding.onlyHorizontal())) {
            val onEventClick = { event: EventUi.Normal -> goToEventDetail(event.occurrenceId) }
            val pagerModifier = Modifier.collapseCalendarOnScroll(calendarExpansionState)
            val headerModifier = Modifier.backgroundBlur(TopAppBarDefaults.topAppBarColors().containerColor, hazeState)
            val timelineModifier = Modifier.hazeSource(hazeState)
            val pagerPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            )

            when (view) {
                MultiDaysView.ThreeDays -> ThreeDaysPager(
                    dateRange = dateRange,
                    selectedDate = { visibleDayState.visibleDate },
                    eventsOf = { eventsByDate()[it] ?: DayEvents.Empty },
                    onVisibleDateChanged = visibleDayState::updateVisibleDate,
                    onEventClick = onEventClick,
                    state = timelineState,
                    columnsState = rememberDayColumnsState(maxVisibleDayCount = 3),
                    modifier = pagerModifier,
                    headerModifier = headerModifier,
                    timelineModifier = timelineModifier,
                    contentPadding = pagerPadding,
                )
                MultiDaysView.Week -> WeekPager(
                    dateRange = dateRange,
                    selectedDate = { visibleDayState.visibleDate },
                    weekNumbering = WeekNumbering.ISO_8601, //TODO[weekNumbering]: Use week numbering from LocalSettings
                    eventsOf = { eventsByDate()[it] ?: DayEvents.Empty },
                    onVisibleDateChanged = visibleDayState::updateVisibleDate,
                    onEventClick = onEventClick,
                    state = timelineState,
                    columnsState = rememberDayColumnsState(maxVisibleDayCount = 7),
                    modifier = pagerModifier,
                    headerModifier = headerModifier,
                    timelineModifier = timelineModifier,
                    contentPadding = pagerPadding,
                )
            }
        }
    }
}

@Preview
@Composable
private fun MultiDaysScreenPreview(@PreviewParameter(MultiDaysViewProvider::class) view: MultiDaysView) {
    CalendarThemeForPreview {
        val visibleDayState = rememberVisibleDayState()
        CompositionLocalProvider(LocalVisibleDayState provides visibleDayState) {
            MultiDaysScreen(
                view = view,
                goToEventDetail = {},
                dateRange = Clock.today().let { it..it.plus(DatePeriod(days = 14)) },
                eventsByDate = { emptyMap() },
                isLoadingEvents = { false },
                eventsDots = { emptyMap() },
                visibleDayState = visibleDayState,
                timelineState = rememberDayTimelineState(),
            )
        }
    }
}

private class MultiDaysViewProvider : PreviewParameterProvider<MultiDaysView> {
    override val values = MultiDaysView.entries.asSequence()
}
