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
package com.infomaniak.calendar.components.day.component

import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.layout.TimedEventsColumn
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.paging.rememberDayPagesFlingBehavior
import com.infomaniak.calendar.components.day.paging.viewportOffsetOf
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayColumnsState
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayColumnsState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.day.zoom.DayStripZoomAxis
import com.infomaniak.calendar.components.day.zoom.pinchToZoom
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.state.rememberCurrentDateTime
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlin.time.Clock

/**
 * The days of [dateRange] side by side in a row, each in its own column of the hour grid, under a
 * single hour gutter. The row scrolls through the days and comes to rest on pages of [daysPerPage]
 * days, see [pageSnapTarget][com.infomaniak.calendar.components.day.paging.pageSnapTarget].
 *
 * How many days the viewport holds is up to [columnsState], which a pinch widens the days through
 * when it allows for it. How many it turns per page is up to [daysPerPage] alone: a week view holds
 * and turns 7 days, while a 3 days view holds 3 days but steps through them one day at a time.
 *
 * @param stripState Where the row of days is scrolled to, its item indices being the days of
 * [dateRange].
 */
@Composable
internal fun MultiDayTimeline(
    dateRange: ClosedRange<LocalDate>,
    stripState: LazyListState,
    columnsState: DayColumnsState,
    daysPerPage: Int,
    eventsOf: (LocalDate) -> DayEvents,
    state: DayTimelineState,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val gutterWidth = DayTimelineDefaults.HourGutterWidth
    val endPadding = DayTimelineDefaults.TimelineEndPadding

    val dayCount = remember(dateRange) { dateRange.start.daysUntil(dateRange.endInclusive) + 1 }
    val flingBehavior = rememberDayPagesFlingBehavior(stripState, columnsState, daysPerPage)
    val stripZoomAxis = remember(columnsState, stripState) {
        if (columnsState.isZoomable) DayStripZoomAxis(columnsState, stripState) else null
    }

    SettleAfterPinch(stripState, flingBehavior, isPinching = { columnsState.isPinching || state.isPinching })

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding.onlyHorizontal()),
    ) {
        val viewportWidth = maxWidth - gutterWidth - endPadding
        val columnWidth = with(LocalDensity.current) { columnsState.columnWidthPx(viewportWidth.roundToPx()).toDp() }

        Box(modifier = Modifier.verticalTimelineScroll(state, contentPadding)) {
            HourLabels(state = state, modifier = Modifier.width(gutterWidth))
            HourLines(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = gutterWidth, end = endPadding),
            )

            LazyRow(
                state = stripState,
                flingBehavior = flingBehavior,
                userScrollEnabled = !columnsState.isPinching && !state.isPinching,
                modifier = Modifier
                    .padding(start = gutterWidth)
                    .width(viewportWidth)
                    .timelineHeight(state)
                    .pinchToZoom(horizontal = stripZoomAxis, vertical = state.zoomAxis),
            ) {
                items(count = dayCount, key = { it }) { index ->
                    DayColumn(
                        events = eventsOf(dateRange.dateOf(index)),
                        columnWidth = columnWidth,
                        state = state,
                        onEventClick = onEventClick,
                    )
                }
            }

            CurrentTimeOverlay(
                dateRange = dateRange,
                stripState = stripState,
                columnWidth = columnWidth,
                state = state,
                modifier = Modifier
                    .matchParentSize()
                    .padding(start = gutterWidth - CurrentTimeDotRadius, end = endPadding),
            )
        }
    }
}

/**
 * Brings the row back to rest once a pinch lets go of it: zooming moves the days without flinging
 * them, so nothing else would snap them back onto a page.
 */
@Composable
private fun SettleAfterPinch(stripState: LazyListState, flingBehavior: TargetedFlingBehavior, isPinching: () -> Boolean) {
    LaunchedEffect(stripState, flingBehavior) {
        snapshotFlow(isPinching).drop(1).filter { !it }.collect {
            stripState.scroll { with(flingBehavior) { performFling(0f) } }
        }
    }
}

@Composable
private fun DayColumn(
    events: DayEvents,
    columnWidth: Dp,
    state: DayTimelineState,
    onEventClick: (EventUi.Normal) -> Unit,
) {
    Box(
        modifier = Modifier
            .width(columnWidth)
            .fillMaxHeight()
            .dayColumnDivider(),
    ) {
        TimedEventsColumn(
            events = events.timed,
            width = columnWidth,
            hourHeight = state.hourHeight,
            onEventClick = onEventClick,
            modifier = Modifier.matchParentSize(),
        )
    }
}

/**
 * The current time across today's column, laid over the row rather than inside it: the row clips
 * its days to its viewport, which would cut the dot of a today lying at its start in half.
 *
 * The overlay clips to its own bounds instead, which are expected to start a dot's radius before
 * the row so that the dot can reach into the gutter.
 */
@Composable
private fun CurrentTimeOverlay(
    dateRange: ClosedRange<LocalDate>,
    stripState: LazyListState,
    columnWidth: Dp,
    state: DayTimelineState,
    modifier: Modifier = Modifier,
) {
    val currentDateTime by rememberCurrentDateTime()
    if (currentDateTime.date !in dateRange) return

    val todayIndex = dateRange.start.daysUntil(currentDateTime.date)

    Box(modifier = modifier.clipToBounds()) {
        CurrentTimeIndicator(
            minuteOfDay = currentDateTime.minuteOfDay,
            state = state,
            modifier = Modifier
                .offset {
                    // Out of view, today's column is left past the end of the clip.
                    val todayOffset = stripState.viewportOffsetOf(todayIndex) ?: stripState.layoutInfo.viewportSize.width
                    IntOffset(x = CurrentTimeDotRadius.roundToPx() + todayOffset, y = 0)
                }
                .width(columnWidth)
                .fillMaxHeight(),
        )
    }
}

private fun ClosedRange<LocalDate>.dateOf(index: Int): LocalDate = start.plus(index, DateTimeUnit.DAY)

private val previewDateRange = Clock.today().let { it..it + DatePeriod(days = 6) }

@Preview
@Composable
private fun MultiDayTimelinePreview() {
    Surface {
        MultiDayTimeline(
            dateRange = previewDateRange,
            stripState = rememberLazyListState(),
            columnsState = rememberDayColumnsState(maxVisibleDayCount = 3),
            daysPerPage = 1,
            eventsOf = { previewDayEvents },
            state = rememberDayTimelineState(scrollState = rememberScrollState(initial = 430)),
            onEventClick = {},
        )
    }
}

@Preview
@Composable
private fun MultiDayTimelineZoomedInPreview() {
    Surface {
        MultiDayTimeline(
            dateRange = previewDateRange,
            stripState = rememberLazyListState(),
            columnsState = rememberDayColumnsState(maxVisibleDayCount = 3).apply { zoom = 2f },
            daysPerPage = 1,
            eventsOf = { previewDayEvents },
            state = rememberDayTimelineState(scrollState = rememberScrollState(initial = 430)),
            onEventClick = {},
        )
    }
}
