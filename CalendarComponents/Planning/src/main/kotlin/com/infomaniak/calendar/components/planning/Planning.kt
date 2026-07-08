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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
    goToEventCreation: () -> Unit,
    onVisibleDateChanged: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val events = weekEvents()
    val sectionSizing = remember { SectionSizing() }

    val keyToDate = remember(events) { events.buildKeyToDateMap() }
    ReportVisibleDate(lazyListState = lazyListState, keyToDate = { keyToDate }, onVisibleDateChanged = onVisibleDateChanged)

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(Margin.Mini),
    ) {
        events.forEach { (week, days) ->
            item(key = week) {
                Text(week.label, modifier = Modifier.padding(vertical = Margin.Medium))
            }

            days.forEach { (date, events) ->
                val sectionItemKeys = events.map { it.itemKey }

                items(events, key = { it.itemKey }) { event ->
                    Row(
                        modifier = Modifier.ensureSectionMinHeight(sectionSizing, sectionItemKeys, event.itemKey),
                        horizontalArrangement = Arrangement.spacedBy(Margin.Small),
                    ) {
                        DayIndicator(
                            dayName = date.toJavaLocalDate().format(shortDayNameFormatter),
                            dayNumber = date.day,
                            state = if (date == today) DateState.Today else DateState.None,
                            modifier = Modifier
                                .measureIndicator(sectionSizing)
                                .stickyDayIndicator(lazyListState, event.itemKey, sectionItemKeys),
                        )

                        when (event) {
                            is EventUi.Normal -> EventItem(event, Modifier.fillMaxWidth())
                            is EventUi.TodayEmptyState -> TodayEmptyState(onClick = goToEventCreation, Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

private fun Map<YearWeek, Map<LocalDate, List<EventUi>>>.buildKeyToDateMap(): Map<Any, LocalDate> = buildMap {
    this@buildKeyToDateMap.forEach { (week, days) ->
        put(week, week.firstDay)
        days.forEach { (date, dayEvents) ->
            dayEvents.forEach { event -> put(event.itemKey, date) }
        }
    }
}

@Composable
private fun ReportVisibleDate(
    lazyListState: LazyListState,
    keyToDate: () -> Map<Any, LocalDate>,
    onVisibleDateChanged: (LocalDate) -> Unit,
) {
    val currentKeyToDate by rememberUpdatedState(keyToDate)

    LaunchedEffect(lazyListState) {
        snapshotFlow { currentKeyToDate()[lazyListState.firstVisibleItemKey()] }
            .mapNotNull { it }
            .distinctUntilChanged()
            .collect { onVisibleDateChanged(it) }
    }
}

private fun LazyListState.firstVisibleItemKey(): Any? = layoutInfo.visibleItemsInfo.firstOrNull { it.offset + it.size > 0 }?.key

private val YearWeek.label: String
    @Composable get() {
        val week = stringResource(R.string.weekHeaderWeekNumber, weekNumber)
        val dateRange = stringResource(R.string.weekHeaderDateRange, firstDay.day, lastDay.day, monthDisplayName, year)
        return "$week - $dateRange"
    }

private val EventUi.itemKey get() = id

@Preview
@Composable
private fun PreviewPlanning(@PreviewParameter(WeekEventsPreviewParameter::class) weekEvents: Map<YearWeek, Map<LocalDate, List<EventUi>>>) {
    Surface {
        Planning(goToEventCreation = {}, weekEvents = { weekEvents }, onVisibleDateChanged = {})
    }
}
