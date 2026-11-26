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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.day.TimelineDefaults
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.model.DayPages
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.event.highlight.EventHighlightState
import com.infomaniak.calendar.components.event.highlight.eventDropTarget
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.core.common.utils.today
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/**
 * The all-day events of the days [pagerState] shows, stacked in their day's column and following the
 * pager as it is swiped. Like the single day's band, it is named from the hour gutter and shows two
 * rows of chips at most, the next one peeking, the rest scrolled to.
 *
 * With a [highlightState], a long press holds a chip up to drag it, and every day's column is where an
 * event can be dropped on that day, all day long.
 */
@Composable
internal fun MultiDayAllDayBand(
    pagerState: PagerState,
    pages: DayPages,
    eventsOf: (LocalDate) -> DayEvents,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    textOverflow: TextOverflow = TextOverflow.Ellipsis,
    highlightState: EventHighlightState? = null,
) {
    Row(modifier = modifier) {
        AllDayLabel()

        Box(modifier = Modifier.weight(1f)) {
            highlightState?.let {
                // The whole height of each day's column, chips or not, and below the scroll, which would
                // only make it as tall as the column's chips.
                DropTargets(pagerState, pages, highlightState = it, modifier = Modifier.matchParentSize())
            }

            PagerAlignedPages(
                pagerState = pagerState,
                modifier = Modifier
                    .heightIn(max = AllDayBandMaxHeight)
                    .verticalScroll(rememberScrollState(), enabled = highlightState?.isHeld != true),
            ) { page ->
                DaysRow(dates = pages.datesOf(page)) { date ->
                    AllDayColumn(
                        date = date,
                        events = eventsOf(date).allDay,
                        onEventClick = onEventClick,
                        textOverflow = textOverflow,
                        highlightState = highlightState,
                    )
                }
            }
        }
    }
}

@Composable
private fun DropTargets(
    pagerState: PagerState,
    pages: DayPages,
    highlightState: EventHighlightState,
    modifier: Modifier = Modifier,
) {
    PagerAlignedPages(pagerState = pagerState, modifier = modifier) { page ->
        DaysRow(dates = pages.datesOf(page), modifier = Modifier.fillMaxHeight()) { date ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .eventDropTarget(highlightState, date),
            )
        }
    }
}

@Composable
private fun AllDayColumn(
    date: LocalDate,
    events: List<EventSummaryUi.Normal>,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    textOverflow: TextOverflow,
    highlightState: EventHighlightState?,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AllDayChipSpacing),
        // The same gap between the days' chips as between their timed events.
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp),
    ) {
        events.forEach { event ->
            key(event.id) {
                AllDayChip(
                    event = event,
                    onClick = { onEventClick(event) },
                    highlightState = highlightState,
                    textOverflow = textOverflow,
                    // An event over several days has a chip in each of them, alike but for their day.
                    highlightKey = date to event,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ThreeDaysPreview() {
    MultiDayAllDayBandSample(daysPerPage = 3, textOverflow = TextOverflow.Ellipsis)
}

/** A week's narrow columns, cut without an ellipsis to keep the room for the few letters that fit. */
@Preview
@Composable
private fun WeekPreview() {
    MultiDayAllDayBandSample(daysPerPage = 7, textOverflow = TextOverflow.Clip)
}

@Composable
private fun MultiDayAllDayBandSample(daysPerPage: Int, textOverflow: TextOverflow) {
    val today = Clock.today()
    val pages = DayPages(today..today.plus(daysPerPage - 1, DateTimeUnit.DAY), daysPerPage)
    val pagerState = rememberPagerState { pages.count }
    val allDay = previewDayEvents.allDay

    Surface {
        Box {
            // The band only follows a pager: this one, empty, stands in for the timeline's.
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .padding(start = TimelineDefaults.HourGutterWidth)
                    .fillMaxWidth()
                    .height(0.dp),
            ) {}

            MultiDayAllDayBand(
                pagerState = pagerState,
                pages = pages,
                // A different number of chips every day, some none, to see the columns fill unevenly.
                eventsOf = { date ->
                    val count = pages.firstDateOf(0).daysUntil(date) * 2 % (allDay.size + 1)
                    DayEvents(allDay = allDay.take(count), timed = emptyList())
                },
                onEventClick = {},
                textOverflow = textOverflow,
            )
        }
    }
}
