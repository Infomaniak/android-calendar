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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.infomaniak.calendar.components.event.EventItem
import com.infomaniak.calendar.components.foundation.component.DateState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.YearWeek
import com.infomaniak.calendar.components.foundation.state.rememberToday
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.DayFormatter.toShortDayName
import com.infomaniak.calendar.components.planning.component.DayIndicator
import com.infomaniak.calendar.components.planning.component.emptyState.OtherDayEmptyState
import com.infomaniak.calendar.components.planning.component.emptyState.TodayEmptyState
import com.infomaniak.calendar.components.planning.preview.PlanningRowPreviewParameter
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

@Composable
fun Planning(
    rows: LazyPagingItems<PlanningRow>,
    goToEventCreation: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    Timeline(
        lazyListState = lazyListState,
        rows = rows,
        goToEventCreation = goToEventCreation,
        contentPadding = contentPadding,
        modifier = modifier,
    )
}

@Composable
private fun Timeline(
    lazyListState: LazyListState,
    rows: LazyPagingItems<PlanningRow>,
    goToEventCreation: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val today by rememberToday()
    val sectionSizing = remember { SectionSizing() }

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(Margin.Mini),
    ) {
        items(
            count = rows.itemCount,
            key = rows.itemKey { it.key },
            contentType = rows.itemContentType { it.contentType },
        ) { index ->
            when (val row = rows[index]) {
                is PlanningRow.WeekHeader -> {
                    Text(row.week.label, modifier = Modifier.padding(bottom = Margin.Medium))
                }
                is PlanningRow.Event -> {
                    Event(
                        event = row.event,
                        date = row.date,
                        today = today,
                        lazyListState = lazyListState,
                        sectionSizing = sectionSizing,
                        itemKey = row.key,
                        sectionItemKeys = row.daySectionKeys,
                        goToEventCreation = goToEventCreation,
                        modifier = Modifier
                            .ensureSectionMinHeight(sectionSizing, row.daySectionKeys, row.key)
                            .padding(bottom = if (row.isLastInDay) Margin.Medium else 0.dp),
                    )
                }
                null -> Unit
            }
        }
    }
}

@Composable
private fun Event(
    event: EventUi,
    date: LocalDate,
    today: LocalDate,
    lazyListState: LazyListState,
    sectionSizing: SectionSizing,
    itemKey: PlanningItemKey,
    sectionItemKeys: List<PlanningItemKey>,
    goToEventCreation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
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
            is EventUi.EmptyState -> OtherDayEmptyState(onClick = goToEventCreation, Modifier.fillMaxWidth())
        }
    }
}

private val YearWeek.label: String
    @Composable get() {
        val week = stringResource(R.string.weekHeaderWeekNumber, weekNumber)
        val dateRange = stringResource(R.string.weekHeaderDateRange, firstDay.day, lastDay.day, monthDisplayName, year)
        return "$week - $dateRange"
    }

@Preview
@Composable
private fun PreviewPlanning(@PreviewParameter(PlanningRowPreviewParameter::class) rows: List<PlanningRow>) {
    Surface {
        Planning(
            goToEventCreation = {},
            rows = flowOf(PagingData.from(rows)).collectAsLazyPagingItems(),
        )
    }
}
