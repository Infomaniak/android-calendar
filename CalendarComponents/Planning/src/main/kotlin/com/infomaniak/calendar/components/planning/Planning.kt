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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.calendar.components.event.EventItem
import com.infomaniak.calendar.components.foundation.component.DateState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.YearWeek
import com.infomaniak.calendar.components.planning.component.DayIndicator
import com.infomaniak.calendar.components.planning.component.TodayEmptyState
import com.infomaniak.calendar.components.planning.preview.WeekEventsPreviewParameter
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.todayIn
import java.time.format.DateTimeFormatter
import kotlin.time.Clock

private val shortDayNameFormatter = DateTimeFormatter.ofPattern("EEE")

@Composable
fun Planning(
    weekEvents: () -> Map<YearWeek, Map<LocalDate, List<EventUi>>>,
    onVisibleDateChanged: (LocalDate) -> Unit,
    goToEventCreation: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val events = weekEvents()

    ReportVisibleDate(lazyListState, events, onVisibleDateChanged)

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(Margin.Large),
    ) {
        events.forEach { (week, days) ->
            item(key = week) {
                Text(week.label)
            }
            days.forEach { (date, dayEvents) ->
                val dayKey = date
                item(key = dayKey) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Margin.Small)) {
                        DayIndicator(
                            dayName = date.toJavaLocalDate().format(shortDayNameFormatter),
                            dayNumber = date.day,
                            state = if (date == today) DateState.Today else DateState.None,
                            modifier = Modifier.stickyWithinItem(lazyListState, dayKey),
                        )
                        EventList(onEventCreation = goToEventCreation, events = dayEvents, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportVisibleDate(
    lazyListState: LazyListState,
    events: Map<YearWeek, Map<LocalDate, List<EventUi>>>,
    onVisibleDateChanged: (LocalDate) -> Unit,
) {
    val allDates = events.values.flatMap { it.keys }

    LaunchedEffect(lazyListState, allDates) {
        snapshotFlow { lazyListState.firstVisibleItemKey() }
            .mapNotNull { key -> allDates.firstOrNull { it == key } }
            .distinctUntilChanged()
            .collect(onVisibleDateChanged)
    }
}

private fun LazyListState.firstVisibleItemKey(): Any? = layoutInfo.visibleItemsInfo.firstOrNull()?.key

@Composable
private fun EventList(onEventCreation: () -> Unit, events: List<EventUi>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Margin.Mini)) {
        events.forEach { event ->
            when (event) {
                is EventUi.Normal -> EventItem(event, modifier = Modifier.fillMaxWidth())
                is EventUi.TodayEmptyState -> TodayEmptyState(onClick = onEventCreation, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private val YearWeek.label: String
    @Composable get() {
        val week = stringResource(R.string.weekHeaderWeekNumber, weekNumber)
        val dateRange = stringResource(R.string.weekHeaderDateRange, firstDay.day, lastDay.day, monthDisplayName, year)
        return "$week - $dateRange"
    }

fun Map<YearWeek, Map<LocalDate, List<EventUi>>>.indexOf(date: LocalDate): Int {
    var index = 0
    entries.forEach { (week, days) ->
        if (date < week.firstDay) return 0
        index++
        if (date <= week.lastDay) return index + days.keys.count { it < date }
        index += days.size
    }
    return 0
}

@Preview
@Composable
private fun PreviewPlanning(@PreviewParameter(WeekEventsPreviewParameter::class) weekEvents: Map<YearWeek, Map<LocalDate, List<EventUi>>>) {
    Surface {
        Planning(goToEventCreation = { }, weekEvents = { weekEvents }, onVisibleDateChanged = { })
    }
}
