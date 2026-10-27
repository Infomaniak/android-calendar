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
package com.infomaniak.calendar.components.planning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.calendar.components.event.EventItem
import com.infomaniak.calendar.components.event.EventItemDefaults
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.models.YearWeek
import com.infomaniak.calendar.components.foundation.state.DateState
import com.infomaniak.calendar.components.foundation.state.rememberToday
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatShortDayNameUppercase
import com.infomaniak.calendar.components.planning.component.DayIndicator
import com.infomaniak.calendar.components.planning.component.emptyState.OtherDayEmptyState
import com.infomaniak.calendar.components.planning.component.emptyState.TodayEmptyState
import com.infomaniak.calendar.components.planning.preview.PlanningPreviewParameter
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.LocalDate

@Composable
fun Planning(
    timeline: PlanningTimeline,
    eventsOf: (LocalDate) -> List<EventSummaryUi>?,
    goToEventCreation: (LocalDate) -> Unit,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val today by rememberToday()

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(Margin.Mini),
    ) {
        items(
            count = timeline.itemCount,
            contentType = { index -> if (timeline.isWeekHeader(index)) ContentType.WeekHeader else ContentType.Day },
        ) { index ->
            if (timeline.isWeekHeader(index)) {
                Text(timeline.weekAt(index).label, modifier = Modifier.padding(bottom = Margin.Medium))
            } else {
                val date = timeline.dateAt(index)
                Day(
                    date = date,
                    events = eventsOf(date),
                    today = today,
                    lazyListState = lazyListState,
                    index = index,
                    goToEventCreation = goToEventCreation,
                    onEventClick = onEventClick,
                )
            }
        }
    }
}

@Composable
private fun Day(
    date: LocalDate,
    events: List<EventSummaryUi>?,
    today: LocalDate,
    lazyListState: LazyListState,
    index: Int,
    goToEventCreation: (LocalDate) -> Unit,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
) {
    Row(
        modifier = Modifier.padding(bottom = Margin.Medium),
        horizontalArrangement = Arrangement.spacedBy(Margin.Small),
    ) {
        DayIndicator(
            dayName = date.formatShortDayNameUppercase(),
            dayNumber = date.day,
            state = if (date == today) DateState.Today else DateState.None,
            modifier = Modifier.stickyDayIndicator(lazyListState, index),
        )

        Column(verticalArrangement = Arrangement.spacedBy(Margin.Mini)) {
            events?.forEach { event ->
                key(event.id) { DayEvent(event, date, goToEventCreation, onEventClick) }
            }
        }
    }
}

@Composable
private fun DayEvent(
    event: EventSummaryUi,
    date: LocalDate,
    goToEventCreation: (LocalDate) -> Unit,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
) {
    when (event) {
        is EventSummaryUi.Normal -> {
            EventItem(
                event = event,
                onClick = { onEventClick(event) },
                modifier = Modifier.fillMaxWidth(),
                allDayTrailingContent = EventItemDefaults.AllDayLabel,
            )
        }
        is EventSummaryUi.TodayEmptyState -> TodayEmptyState(onClick = { goToEventCreation(date) }, Modifier.fillMaxWidth())
        is EventSummaryUi.EmptyState -> OtherDayEmptyState(onClick = { goToEventCreation(date) }, Modifier.fillMaxWidth())
    }
}

private enum class ContentType { WeekHeader, Day }

private val YearWeek.label: String
    @Composable get() {
        val week = stringResource(R.string.weekHeaderWeekNumber, weekNumber)
        val dateRange = stringResource(R.string.weekHeaderDateRange, firstDay.day, lastDay.day, monthDisplayName, year)
        return "$week - $dateRange"
    }

@Preview
@Composable
private fun PreviewPlanning(
    @PreviewParameter(PlanningPreviewParameter::class) eventsByDate: Map<LocalDate, List<EventSummaryUi>>,
) {
    val timeline = remember { PlanningTimeline(WeekNumbering.ISO_8601) }

    Surface {
        Planning(
            timeline = timeline,
            eventsOf = eventsByDate::get,
            goToEventCreation = {},
            onEventClick = {},
            lazyListState = rememberLazyListState(timeline.indexOf(eventsByDate.keys.min())),
        )
    }
}
