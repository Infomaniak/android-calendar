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
package com.infomaniak.calendar.components.day

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.day.component.CurrentTimeIndicator
import com.infomaniak.calendar.components.day.component.DayColumnHeader
import com.infomaniak.calendar.components.day.component.FollowSelectedDate
import com.infomaniak.calendar.components.day.component.HourLabelOverhang
import com.infomaniak.calendar.components.day.component.HourLabels
import com.infomaniak.calendar.components.day.component.HourLines
import com.infomaniak.calendar.components.day.component.minuteOfDay
import com.infomaniak.calendar.components.day.component.verticalTimelineScroll
import com.infomaniak.calendar.components.day.highlight.EventHighlightState
import com.infomaniak.calendar.components.day.highlight.HighlightedEventOverlay
import com.infomaniak.calendar.components.day.highlight.eventHighlightArea
import com.infomaniak.calendar.components.day.highlight.releaseHighlightOnSecondPointer
import com.infomaniak.calendar.components.day.highlight.rememberEventHighlightState
import com.infomaniak.calendar.components.day.layout.EventLayoutDefaults.GridLineThickness
import com.infomaniak.calendar.components.day.layout.TimedEventsColumn
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.model.DayPages
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.TimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.state.rememberCurrentDateTime
import com.infomaniak.calendar.components.foundation.theme.ComponentColors
import com.infomaniak.calendar.components.foundation.utils.onlyHorizontalAndTop
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Clock

/**
 * Several days side by side, [visibleDayCount] columns on screen, swiped [daysPerPage] days at a time.
 *
 * Pages are counted from [dateRange]'s start: each holds the [daysPerPage] days following the previous
 * one, so a range starting on the first day of a week, swiped a week at a time, always shows whole
 * weeks. The hour gutter stays in place while the days move, and the day headers follow them.
 *
 * The first day of the page in view is reported through [onVisibleDateChanged], unless that page
 * already holds [selectedDate]; a [selectedDate] off screen brings its page into view.
 *
 * @param eventTextOverflow How the events' texts are cut. Narrow columns show more of them with
 * [TextOverflow.Clip], the ellipsis taking the room of the letters it replaces.
 */
@Composable
fun MultiDayPager(
    dateRange: ClosedRange<LocalDate>,
    visibleDayCount: Int,
    daysPerPage: Int,
    selectedDate: () -> LocalDate,
    eventsOf: (LocalDate) -> DayEvents,
    state: TimelineState,
    weekNumbering: WeekNumbering,
    onVisibleDateChanged: (LocalDate) -> Unit,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    headerModifier: Modifier = Modifier,
    timelineModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    eventTextOverflow: TextOverflow = TextOverflow.Ellipsis,
) {
    require(daysPerPage in 1..visibleDayCount) { "daysPerPage must be between 1 and visibleDayCount" }

    val pages = remember(dateRange, daysPerPage) { DayPages(dateRange, daysPerPage) }

    // A pager state counts pages, so it can't carry its position over to pages of another size.
    key(pages) {
        val pagerState = rememberPagerState(initialPage = pages.pageOf(selectedDate())) { pages.count }

        FollowSelectedDate(pagerState, pages, selectedDate, onVisibleDateChanged)

        MultiDayPagerContent(
            pagerState = pagerState,
            pages = pages,
            visibleDayCount = visibleDayCount,
            eventsOf = eventsOf,
            state = state,
            weekNumbering = weekNumbering,
            onEventClick = onEventClick,
            modifier = modifier,
            headerModifier = headerModifier,
            timelineModifier = timelineModifier,
            contentPadding = contentPadding,
            eventTextOverflow = eventTextOverflow,
        )
    }
}

@Composable
private fun MultiDayPagerContent(
    pagerState: PagerState,
    pages: DayPages,
    visibleDayCount: Int,
    eventsOf: (LocalDate) -> DayEvents,
    state: TimelineState,
    weekNumbering: WeekNumbering,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    headerModifier: Modifier = Modifier,
    timelineModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    eventTextOverflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(0.dp) }
    // The grid runs to the end edge, keeping all the room it can for the days' content.
    val contentPadding = contentPadding + PaddingValues(start = EsdsTheme.spacing.md)
    val pageSize = remember(pages, visibleDayCount) { DaysPageSize(pages.daysPerPage, visibleDayCount) }
    val highlightState = rememberEventHighlightState()

    Box(modifier.releaseHighlightOnSecondPointer(highlightState)) {
        MultiDayTimeline(
            pagerState = pagerState,
            pages = pages,
            pageSize = pageSize,
            eventsOf = eventsOf,
            state = state,
            highlightState = highlightState,
            onEventClick = onEventClick,
            eventTextOverflow = eventTextOverflow,
            contentPadding = contentPadding + PaddingValues(top = headerHeight + EsdsTheme.spacing.md),
            modifier = timelineModifier.fillMaxSize(),
        )

        OverlaidHeaders(
            pagerState = pagerState,
            pages = pages,
            weekNumbering = weekNumbering,
            onHeightChanged = { headerHeight = with(density) { it.toDp() } },
            modifier = headerModifier.fillMaxWidth(),
            contentPadding = contentPadding.onlyHorizontalAndTop(),
        )

        HighlightedEventOverlay(highlightState, modifier = Modifier.matchParentSize())
    }
}

/**
 * The hour gutter, in place, beside the pager carrying the days' hour grid, one column per day.
 *
 * The gutter and every page scroll vertically on their own, all on the same [state], so they move as
 * one. A pinch is caught above them all, since its fingers can land on two different pages.
 *
 * An event held up by a long press unfolds across the whole pager, which stays put until it is let go.
 */
@Composable
private fun MultiDayTimeline(
    pagerState: PagerState,
    pages: DayPages,
    pageSize: PageSize,
    eventsOf: (LocalDate) -> DayEvents,
    state: TimelineState,
    highlightState: EventHighlightState,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    eventTextOverflow: TextOverflow,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val currentDateTime by rememberCurrentDateTime()
    val gridTop by rememberUpdatedState(HourLabelOverhang + contentPadding.calculateTopPadding())

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding.onlyHorizontal())
            .pinchToZoom(state, PointerEventPass.Initial) { y -> y - gridTop.toPx() + state.scrollState.value },
    ) {
        HourLabels(
            state = state,
            modifier = Modifier
                .width(TimelineDefaults.HourGutterWidth)
                .fillMaxHeight()
                .verticalTimelineScroll(state, contentPadding, overscrollEffect = null, enabled = !highlightState.isHeld)
                .verticalEndDivider(),
        )

        HorizontalPager(
            state = pagerState,
            pageSize = pageSize,
            userScrollEnabled = !state.isPinching && !highlightState.isHeld,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .eventHighlightArea(),
        ) { page ->
            DaysRow(
                dates = pages.datesOf(page),
                modifier = Modifier.verticalTimelineScroll(
                    state = state,
                    contentPadding = contentPadding,
                    overscrollEffect = null,
                    enabled = !highlightState.isHeld,
                ),
            ) { date ->
                Box(modifier = Modifier.verticalEndDivider()) {
                    HourLines(state = state, modifier = Modifier.fillMaxWidth())

                    TimedEventsColumn(
                        events = eventsOf(date).timed,
                        hourHeight = state.hourHeight,
                        onEventClick = onEventClick,
                        modifier = Modifier
                            .matchParentSize()
                            .padding(horizontal = 1.dp),
                        textOverflow = eventTextOverflow,
                        highlightState = highlightState,
                    )

                    if (currentDateTime.date == date) {
                        // Inside its column: the pager clips whatever overhangs the first one, and every
                        // column holds it the same way so it does not shift as the days are swiped.
                        CurrentTimeIndicator(
                            minuteOfDay = currentDateTime.minuteOfDay,
                            state = state,
                            modifier = Modifier.matchParentSize(),
                            isDotInside = true,
                        )
                    }
                }
            }
        }
    }
}

/** The week number over the hour gutter, then the headers of the days in view, following the pager. */
@Composable
private fun OverlaidHeaders(
    pagerState: PagerState,
    pages: DayPages,
    weekNumbering: WeekNumbering,
    onHeightChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val firstVisibleDate = pages.firstDateOf(pagerState.currentPage)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(contentPadding)
            // Measured inside the padding: the timeline already makes room for it on its own.
            .onSizeChanged { onHeightChanged(it.height) },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.width(TimelineDefaults.HourGutterWidth)) {
            Text(
                text = stringResource(R.string.weekHeaderWeekNumber, weekNumbering.weekOf(firstVisibleDate).weekNumber),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        PagedDayHeaders(pagerState = pagerState, pages = pages, modifier = Modifier.weight(1f))
    }
}

/**
 * The headers of the pages the pager shows, each placed where the pager places its page.
 *
 * They are not a pager of their own: two pagers could only be kept in step after the fact, a frame
 * late. Here the pager's own layout positions them.
 */
@Composable
private fun PagedDayHeaders(pagerState: PagerState, pages: DayPages, modifier: Modifier = Modifier) {
    val visiblePages by remember(pagerState) {
        derivedStateOf { pagerState.layoutInfo.visiblePagesInfo.map { it.index } }
    }

    Layout(
        modifier = modifier.clipToBounds(),
        content = {
            visiblePages.forEach { page ->
                key(page) {
                    DaysRow(dates = pages.datesOf(page)) { date ->
                        DayColumnHeader(date, modifier = Modifier.padding(bottom = EsdsTheme.spacing.xl))
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val layoutInfo = pagerState.layoutInfo
        val placeables = measurables.map { it.measure(Constraints.fixedWidth(layoutInfo.pageSize.coerceAtLeast(0))) }
        val height = placeables.maxOfOrNull { it.height } ?: 0

        layout(constraints.maxWidth, height) {
            placeables.forEachIndexed { index, placeable ->
                val pageInfo = layoutInfo.visiblePagesInfo.firstOrNull { it.index == visiblePages.getOrNull(index) }
                pageInfo?.let { placeable.placeRelative(x = it.offset, y = 0) }
            }
        }
    }
}

/** One equally wide column per date. */
@Composable
private fun DaysRow(
    dates: List<LocalDate>,
    modifier: Modifier = Modifier,
    content: @Composable (LocalDate) -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        dates.forEach { date -> DayColumn { content(date) } }
    }
}

@Composable
private fun RowScope.DayColumn(content: @Composable () -> Unit) {
    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.weight(1f)) {
        content()
    }
}

/** A page of [daysPerPage] days, as wide as that many of the [visibleDayCount] columns on screen. */
private class DaysPageSize(private val daysPerPage: Int, private val visibleDayCount: Int) : PageSize {
    override fun Density.calculateMainAxisPageSize(availableSpace: Int, pageSpacing: Int): Int {
        return availableSpace * daysPerPage / visibleDayCount
    }
}

/** A vertical line, drawn at the end of the component */
@Composable
private fun Modifier.verticalEndDivider(color: Color = ComponentColors.GridDividerColor): Modifier = drawBehind {
    val x = if (layoutDirection == LayoutDirection.Ltr) size.width else 0f
    drawLine(
        color = color,
        start = Offset(x, 0f),
        end = Offset(x, size.height),
        strokeWidth = GridLineThickness.toPx(),
    )
}

@Preview
@Composable
private fun MultiDayPagerPreview() {
    val today = Clock.today()

    Surface {
        MultiDayPager(
            dateRange = today.minus(7, DateTimeUnit.DAY)..today.plus(7, DateTimeUnit.DAY),
            visibleDayCount = 3,
            daysPerPage = 1,
            selectedDate = { today },
            eventsOf = { previewDayEvents },
            state = rememberDayTimelineState({}, scrollState = rememberScrollState(initial = 430)),
            weekNumbering = WeekNumbering.ISO_8601,
            onVisibleDateChanged = {},
            headerModifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            onEventClick = {},
        )
    }
}
