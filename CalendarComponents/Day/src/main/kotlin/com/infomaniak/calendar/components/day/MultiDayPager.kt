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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.day.component.CurrentTimeIndicator
import com.infomaniak.calendar.components.day.component.DayColumnHeader
import com.infomaniak.calendar.components.day.component.DaysRow
import com.infomaniak.calendar.components.day.component.HourLabelOverhang
import com.infomaniak.calendar.components.day.component.HourLabels
import com.infomaniak.calendar.components.day.component.HourLines
import com.infomaniak.calendar.components.day.component.PagerAlignedPages
import com.infomaniak.calendar.components.day.component.allday.MultiDayAllDayBand
import com.infomaniak.calendar.components.day.component.minuteOfDay
import com.infomaniak.calendar.components.day.component.rememberDayHighlightState
import com.infomaniak.calendar.components.day.component.timelineDropTarget
import com.infomaniak.calendar.components.day.component.verticalTimelineScroll
import com.infomaniak.calendar.components.day.layout.TimedEventsColumn
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.model.DayPages
import com.infomaniak.calendar.components.day.model.EventMove
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.TimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.event.highlight.EventHighlightState
import com.infomaniak.calendar.components.event.highlight.HighlightedEventOverlay
import com.infomaniak.calendar.components.event.highlight.eventHighlightArea
import com.infomaniak.calendar.components.event.highlight.releaseHighlightOnSecondPointer
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.state.rememberCurrentDateTime
import com.infomaniak.calendar.components.foundation.utils.FollowSelectedPage
import com.infomaniak.calendar.components.foundation.utils.HorizontalGridLine
import com.infomaniak.calendar.components.foundation.utils.onlyHorizontalAndTop
import com.infomaniak.calendar.components.foundation.utils.verticalEndDivider
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

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
 * @param onEventMoved Called when an event held up by a long press, timed or all-day, is dragged and
 * dropped: on an hour grid, an [EventMove.Timed] starting on the day under the finger at the quarter of an
 * hour nearest to the card's top; on the all-day events, an [EventMove.AllDay] starting on the day under the
 * finger. Whichever of its days is grabbed, the dragged card is the event's first day, its start under the finger.
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
    onEventMoved: ((EventSummaryUi.Normal, EventMove) -> Unit)? = null,
) {
    require(daysPerPage in 1..visibleDayCount) { "daysPerPage must be between 1 and visibleDayCount" }

    val pages = remember(dateRange, daysPerPage) { DayPages(dateRange, daysPerPage) }

    // A pager state counts pages, so it can't carry its position over to pages of another size.
    key(pages) {
        val pagerState = rememberPagerState(initialPage = pages.pageOf(selectedDate())) { pages.count }

        FollowSelectedPage(
            pagerState = pagerState,
            selectedPage = { pages.pageOf(selectedDate()) },
            onPageChanged = { onVisibleDateChanged(pages.firstDateOf(it)) },
        )

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
            onEventMoved = onEventMoved,
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
    onEventMoved: ((EventSummaryUi.Normal, EventMove) -> Unit)? = null,
) {
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(0.dp) }
    // The grid runs to the end edge, keeping all the room it can for the days' content.
    val contentPadding = contentPadding + PaddingValues(start = EsdsTheme.spacing.md)
    val pageSize = remember(pages, visibleDayCount) { DaysPageSize(pages.daysPerPage, visibleDayCount) }
    val highlightState = rememberDayHighlightState(onEventMoved)

    // Where a held all-day chip unfolds: across the whole view, since its band is too short to show it, but
    // beside the hour gutter, like a held timed event.
    val allDayHighlightPadding = contentPadding.onlyHorizontal() + PaddingValues(start = TimelineDefaults.HourGutterWidth)
    Box(modifier.releaseHighlightOnSecondPointer(highlightState).eventHighlightArea(allDayHighlightPadding)) {
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
            eventsOf = eventsOf,
            weekNumbering = weekNumbering,
            onEventClick = onEventClick,
            eventTextOverflow = eventTextOverflow,
            highlightState = highlightState,
            onHeightChanged = { headerHeight = with(density) { it.toDp() } },
            modifier = headerModifier.fillMaxWidth(),
            contentPadding = contentPadding.onlyHorizontalAndTop(),
        )

        // Kept under the headers, which hide whatever part of the grid runs under them.
        HighlightedEventOverlay(
            state = highlightState,
            modifier = Modifier.matchParentSize(),
            contentPadding = contentPadding + PaddingValues(top = headerHeight),
        )
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
                            .padding(horizontal = 1.dp)
                            .timelineDropTarget(highlightState, date),
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

/**
 * The week number over the hour gutter, then the headers of the days in view, and under them the
 * band of their all-day events, both following the pager.
 */
@Composable
private fun OverlaidHeaders(
    pagerState: PagerState,
    pages: DayPages,
    eventsOf: (LocalDate) -> DayEvents,
    weekNumbering: WeekNumbering,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    eventTextOverflow: TextOverflow,
    highlightState: EventHighlightState,
    onHeightChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val firstVisibleDate = pages.firstDateOf(pagerState.currentPage)
    val currentEventsOf by rememberUpdatedState(eventsOf)
    // Like the single day's band, only shown when there is something in it, as soon as a day holding
    // all-day events starts coming into view.
    val hasAllDayEvents by remember(pagerState, pages) {
        derivedStateOf {
            pagerState.layoutInfo.visiblePagesInfo.any { page ->
                pages.datesOf(page.index).any { currentEventsOf(it).allDay.isNotEmpty() }
            }
        }
    }

    Column(
        modifier = modifier
            .padding(top = contentPadding.calculateTopPadding())
            // Measured below the top padding, which the timeline already makes room for on its own,
            // and around the band, so the timeline also starts below it.
            .onSizeChanged { onHeightChanged(it.height) },
    ) {
        Column(modifier = Modifier.padding(contentPadding.onlyHorizontal())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.width(TimelineDefaults.HourGutterWidth)) {
                    Text(
                        text = stringResource(
                            R.string.weekHeaderWeekNumber,
                            weekNumbering.weekOf(firstVisibleDate).weekNumber,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                PagerAlignedPages(pagerState = pagerState, modifier = Modifier.weight(1f)) { page ->
                    DaysRow(dates = pages.datesOf(page)) { date ->
                        DayColumnHeader(date, modifier = Modifier.padding(bottom = EsdsTheme.spacing.xl))
                    }
                }
            }

            if (hasAllDayEvents) {
                MultiDayAllDayBand(
                    pagerState = pagerState,
                    pages = pages,
                    eventsOf = eventsOf,
                    onEventClick = onEventClick,
                    textOverflow = eventTextOverflow,
                    highlightState = highlightState,
                    modifier = Modifier.padding(bottom = EsdsTheme.spacing.xl),
                )
            }
        }

        if (hasAllDayEvents) HorizontalGridLine()
    }
}

/** A page of [daysPerPage] days, as wide as that many of the [visibleDayCount] columns on screen. */
private class DaysPageSize(private val daysPerPage: Int, private val visibleDayCount: Int) : PageSize {
    override fun Density.calculateMainAxisPageSize(availableSpace: Int, pageSpacing: Int): Int {
        return availableSpace * daysPerPage / visibleDayCount
    }
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
