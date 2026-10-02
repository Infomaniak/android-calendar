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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.layout.TimedEventsColumn
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.model.dateAt
import com.infomaniak.calendar.components.day.model.dayCount
import com.infomaniak.calendar.components.day.model.indexOf
import com.infomaniak.calendar.components.day.paging.rememberDayPagesFlingBehavior
import com.infomaniak.calendar.components.day.paging.viewportOffsetOf
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayColumnsState
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayColumnsState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.day.zoom.DayStripZoomAxis
import com.infomaniak.calendar.components.day.zoom.offsetBy
import com.infomaniak.calendar.components.day.zoom.pinchToZoom
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.state.rememberCurrentDateTime
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.max
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
 * Each column is an item of the row, made of its [header] laid over its own vertical viewport of the
 * grid: the header moves along with its day without any delay, while staying at the top. All the
 * viewports and the gutter scroll the same [state], and so as one. The grid scrolls under the
 * headers, all as tall as the tallest of them.
 *
 * @param stripState Where the row of days is scrolled to, its item indices being the days of
 * [dateRange].
 * @param headerModifier Applied to each header and to the corner above the gutter, typically to give
 * them a background the grid scrolls under.
 * @param timelineModifier Applied to each vertical viewport of the grid and to the gutter.
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
    headerModifier: Modifier = Modifier,
    timelineModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    header: @Composable (LocalDate) -> Unit = { DayColumnHeader(date = it) },
) {
    val density = LocalDensity.current
    val gutterWidth = DayTimelineDefaults.HourGutterWidth
    val endPadding = DayTimelineDefaults.TimelineEndPadding

    val dayCount = remember(dateRange) { dateRange.dayCount }
    val flingBehavior = rememberDayPagesFlingBehavior(stripState, columnsState, daysPerPage)

    // Never shrinks, so that the tallest header seen so far sets the height of all of them, the ones
    // composed later included.
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeight = with(density) { headerHeightPx.toDp() }
    val headersTop = contentPadding.calculateTopPadding()
    val gridPadding = PaddingValues(top = headersTop + headerHeight, bottom = contentPadding.calculateBottomPadding())

    // Where the first hour line lies below the top of the pinched area, when scrolled to the top.
    val hourZeroTopPx by rememberUpdatedState(with(density) { (headersTop + HourLabelOverhang).toPx() })
    val gutterWidthPx by rememberUpdatedState(with(density) { gutterWidth.toPx() })

    val stripZoomAxis = remember(columnsState, stripState) {
        if (columnsState.isZoomable) DayStripZoomAxis(columnsState, stripState).offsetBy { -gutterWidthPx } else null
    }
    val hoursZoomAxis = remember(state) {
        state.zoomAxis.offsetBy { state.scrollState.value - hourZeroTopPx - headerHeightPx }
    }

    SettleAfterPinch(stripState, flingBehavior, isPinching = { columnsState.isPinching || state.isPinching })

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding.onlyHorizontal()),
    ) {
        val viewportWidth = maxWidth - gutterWidth - endPadding
        val columnWidth = with(density) { columnsState.columnWidthPx(viewportWidth.roundToPx()).toDp() }

        // Over the gutter and the headers as well, the pinch is caught wherever the fingers land.
        Box(modifier = Modifier.fillMaxSize().pinchToZoom(horizontal = stripZoomAxis, vertical = hoursZoomAxis)) {
            Box(
                modifier = timelineModifier
                    .width(gutterWidth)
                    .fillMaxHeight()
                    .verticalTimelineScroll(state, gridPadding, overscrollEffect = null),
            ) {
                HourLabels(state = state, modifier = Modifier.fillMaxWidth())
            }

            LazyRow(
                state = stripState,
                flingBehavior = flingBehavior,
                userScrollEnabled = !columnsState.isPinching && !state.isPinching,
                modifier = Modifier
                    .padding(start = gutterWidth)
                    .width(viewportWidth)
                    .fillMaxHeight(),
            ) {
                items(count = dayCount, key = { it }) { index ->
                    val date = dateRange.dateAt(index)

                    Box(modifier = Modifier.width(columnWidth).fillMaxHeight()) {
                        DayColumn(
                            events = eventsOf(date),
                            columnWidth = columnWidth,
                            state = state,
                            onEventClick = onEventClick,
                            modifier = timelineModifier
                                .fillMaxSize()
                                .verticalTimelineScroll(state, gridPadding, overscrollEffect = null),
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(top = headersTop)
                                .then(headerModifier)
                                .fillMaxWidth()
                                .heightIn(min = headerHeight)
                                .onSizeChanged { headerHeightPx = max(headerHeightPx, it.height) },
                        ) {
                            header(date)
                        }
                    }
                }
            }

            CurrentTimeOverlay(
                dateRange = dateRange,
                stripState = stripState,
                columnWidth = columnWidth,
                state = state,
                modifier = Modifier
                    .matchParentSize()
                    .padding(start = gutterWidth - CurrentTimeDotRadius, end = endPadding, top = headersTop + headerHeight),
            )

            // The gutter's share of the header row, for the hours not to show through beside the headers.
            Box(
                modifier = Modifier
                    .padding(top = headersTop)
                    .then(headerModifier)
                    .width(gutterWidth)
                    .height(headerHeight),
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

/** A day's events over its share of the grid, to be laid in a vertical scroll. */
@Composable
private fun DayColumn(
    events: DayEvents,
    columnWidth: Dp,
    state: DayTimelineState,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .timelineHeight(state)
                .fillMaxWidth()
                .dayColumnDivider(),
        ) {
            HourLines(state = state, modifier = Modifier.fillMaxWidth())
            TimedEventsColumn(
                events = events.timed,
                width = columnWidth,
                hourHeight = state.hourHeight,
                onEventClick = onEventClick,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

/**
 * The current time across today's column, laid over the row rather than inside it: the row clips
 * its days to its viewport, which would cut the dot of a today lying at its start in half.
 *
 * The overlay clips to its own bounds instead, which are expected to start a dot's radius before
 * the row so that the dot can reach into the gutter, and below the headers.
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

    val todayIndex = dateRange.indexOf(currentDateTime.date)
    val hourZeroTop = HourLabelOverhang

    Box(modifier = modifier.clipToBounds()) {
        CurrentTimeIndicator(
            minuteOfDay = currentDateTime.minuteOfDay,
            state = state,
            modifier = Modifier
                .offset {
                    // Out of view, today's column is left past the end of the clip.
                    val todayOffset = stripState.viewportOffsetOf(todayIndex) ?: stripState.layoutInfo.viewportSize.width
                    IntOffset(
                        x = CurrentTimeDotRadius.roundToPx() + todayOffset,
                        y = hourZeroTop.roundToPx() - state.scrollState.value,
                    )
                }
                .width(columnWidth)
                // Taller than the overlay, the grid would otherwise be centred in it.
                .wrapContentHeight(Alignment.Top, unbounded = true)
                .timelineHeight(state),
        )
    }
}

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
