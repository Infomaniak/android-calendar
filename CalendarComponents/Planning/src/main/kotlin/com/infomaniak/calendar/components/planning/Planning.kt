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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.calendar.components.calendar.component.ExpandableCalendar
import com.infomaniak.calendar.components.event.EventItem
import com.infomaniak.calendar.components.foundation.component.DateState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.YearWeek
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.DayFormatter.toShortDayName
import com.infomaniak.calendar.components.planning.component.DayIndicator
import com.infomaniak.calendar.components.planning.component.TodayEmptyState
import com.infomaniak.calendar.components.planning.preview.WeekEventsPreviewParameter
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Composable
fun Planning(
    weekEvents: () -> Map<YearWeek, Map<LocalDate, List<EventUi>>>,
    goToEventCreation: () -> Unit,
    onJump: (LocalDate) -> Unit,
    currentDay: () -> LocalDate,
    isCalendarExpanded: () -> Boolean,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(modifier = modifier) {
        ExpandableCalendar(
            isExpanded = isCalendarExpanded,
            onDayClick = onJump,
            selectedDate = currentDay,
        )

        Timeline(
            weekEvents = weekEvents,
            lazyListState = lazyListState,
            contentPadding = contentPadding,
            goToEventCreation = goToEventCreation,
        )
    }
}

@Composable
private fun Timeline(
    weekEvents: () -> Map<YearWeek, Map<LocalDate, List<EventUi>>>,
    lazyListState: LazyListState,
    contentPadding: PaddingValues,
    goToEventCreation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = Clock.today()
    val events = weekEvents()
    val sectionSizing = remember { SectionSizing() }

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(Margin.Mini),
    ) {
        events.forEach { (week, days) ->
            item(key = PlanningItemKey.WeekHeader(week.firstDay)) {
                Text(week.label, modifier = Modifier.padding(vertical = Margin.Medium))
            }

            days.forEach { (date, events) ->
                val sectionItemKeys = events.map { it.toItemKey(date) }

                items(events, key = { it.toItemKey(date) }) { event ->
                    val itemKey = event.toItemKey(date)
                    Row(
                        modifier = Modifier.ensureSectionMinHeight(sectionSizing, sectionItemKeys, itemKey),
                        horizontalArrangement = Arrangement.spacedBy(Margin.Small),
                    ) {
                        DayIndicator(
                            dayName = date.toShortDayName(),
                            dayNumber = date.day,
                            state = if (date == today) DateState.Today else DateState.None,
                            modifier = Modifier
                                .measureIndicator(sectionSizing)
                                .stickyDayIndicator(lazyListState, itemKey, sectionItemKeys),
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

private val YearWeek.label: String
    @Composable get() {
        val week = stringResource(R.string.weekHeaderWeekNumber, weekNumber)
        val dateRange = stringResource(R.string.weekHeaderDateRange, firstDay.day, lastDay.day, monthDisplayName, year)
        return "$week - $dateRange"
    }

private fun EventUi.toItemKey(date: LocalDate): PlanningItemKey = PlanningItemKey.Event(date = date, id = id)

@Preview
@Composable
private fun PreviewPlanning(@PreviewParameter(WeekEventsPreviewParameter::class) weekEvents: Map<YearWeek, Map<LocalDate, List<EventUi>>>) {
    Surface {
        Planning(
            goToEventCreation = {},
            weekEvents = { weekEvents },
            onJump = { },
            currentDay = { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
            isCalendarExpanded = { true },
        )
    }
}
