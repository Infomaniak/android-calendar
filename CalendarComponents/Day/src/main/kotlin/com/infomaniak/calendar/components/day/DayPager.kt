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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.day.component.DayView
import com.infomaniak.calendar.components.day.component.timelineDropHandler
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.model.DayPages
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.TimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.event.highlight.HighlightedEventOverlay
import com.infomaniak.calendar.components.event.highlight.releaseHighlightOnSecondPointer
import com.infomaniak.calendar.components.event.highlight.rememberEventHighlightState
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.utils.FollowSelectedPage
import com.infomaniak.core.common.utils.today
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * One day per page, swiped a day at a time.
 *
 * @param onEventMoved Called when an event held up by a long press is dragged and dropped, with where it now
 * starts: the quarter of an hour nearest to the dragged card's top. Whichever of its days is grabbed, the dragged
 * card is the event's first day, its start under the finger.
 */
@Composable
fun DayPager(
    dateRange: ClosedRange<LocalDate>,
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
    headerTrailingContent: @Composable () -> Unit = {},
    onEventMoved: ((EventSummaryUi.Normal, newStart: LocalDateTime) -> Unit)? = null,
) {
    val pages = remember(dateRange) { DayPages(dateRange, daysPerPage = 1) }
    val pagerState = rememberPagerState(initialPage = pages.pageOf(selectedDate())) { pages.count }

    FollowSelectedPage(
        pagerState = pagerState,
        selectedPage = { pages.pageOf(selectedDate()) },
        onPageChanged = { onVisibleDateChanged(pages.firstDateOf(it)) },
    )

    val highlightState = rememberEventHighlightState(
        onEventDropped = onEventMoved?.let(::timelineDropHandler),
    )

    // Each page's header height, by page, so the highlighted event of the shown one is kept under its header.
    val headerHeights = remember { mutableStateMapOf<Int, Dp>() }

    // The highlighted event is drawn over the pager, so neither the page edge nor the header hides it.
    Box(modifier = modifier.releaseHighlightOnSecondPointer(highlightState), propagateMinConstraints = true) {
        HorizontalPager(state = pagerState, userScrollEnabled = !highlightState.isHeld) { page ->
            val date = pages.firstDateOf(page)

            DayView(
                date = date,
                events = eventsOf(date),
                state = state,
                highlightState = highlightState,
                weekNumbering = weekNumbering,
                onEventClick = onEventClick,
                headerModifier = headerModifier,
                timelineModifier = timelineModifier,
                contentPadding = contentPadding,
                headerTrailingContent = headerTrailingContent,
                onHeaderHeightChanged = { headerHeights[page] = it },
            )

            DisposableEffect(page) { onDispose { headerHeights.remove(page) } }
        }

        HighlightedEventOverlay(
            state = highlightState,
            modifier = Modifier.matchParentSize(),
            contentPadding = contentPadding + PaddingValues(top = headerHeights[pagerState.currentPage] ?: 0.dp),
        )
    }
}

@Preview
@Composable
private fun DayPagerPreview() {
    val today = Clock.today()

    Surface {
        DayPager(
            dateRange = today.minus(1, DateTimeUnit.DAY)..today.plus(1, DateTimeUnit.DAY),
            selectedDate = { today },
            eventsOf = { previewDayEvents },
            state = rememberDayTimelineState({}),
            weekNumbering = WeekNumbering.ISO_8601,
            onVisibleDateChanged = {},
            onEventClick = {},
        )
    }
}
