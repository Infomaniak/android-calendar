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

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.core.common.utils.today
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Clock

@Composable
fun DayPager(
    dateRange: ClosedRange<LocalDate>,
    selectedDate: () -> LocalDate,
    eventsOf: (LocalDate) -> DayEvents,
    state: DayTimelineState,
    weekNumbering: WeekNumbering,
    onVisibleDateChanged: (LocalDate) -> Unit,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    headerTrailingContent: @Composable () -> Unit = {},
) {
    val pageCount = remember(dateRange) { dateRange.dayCount }
    val pagerState = rememberPagerState(initialPage = dateRange.pageOf(selectedDate())) { pageCount }

    LaunchedEffect(pagerState) {
        snapshotFlow { selectedDate() }.collectLatest { date ->
            val page = dateRange.pageOf(date)
            if (page != pagerState.currentPage) pagerState.animateScrollToPage(page)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { onVisibleDateChanged(dateRange.dateOf(it)) }
    }

    HorizontalPager(state = pagerState, modifier = modifier) { page ->
        val date = dateRange.dateOf(page)

        DayView(
            date = date,
            events = eventsOf(date),
            state = state,
            weekNumbering = weekNumbering,
            onEventClick = onEventClick,
            headerTrailingContent = headerTrailingContent,
        )
    }
}

private val ClosedRange<LocalDate>.dayCount: Int get() = start.daysUntil(endInclusive) + 1
private fun ClosedRange<LocalDate>.pageOf(date: LocalDate): Int = start.daysUntil(date)
private fun ClosedRange<LocalDate>.dateOf(page: Int): LocalDate = start.plus(page, DateTimeUnit.DAY)

@Preview
@Composable
private fun DayPagerPreview() {
    val today = Clock.today()

    Surface {
        DayPager(
            dateRange = today.minus(1, DateTimeUnit.DAY)..today.plus(1, DateTimeUnit.DAY),
            selectedDate = { today },
            eventsOf = { previewDayEvents },
            state = rememberDayTimelineState(),
            weekNumbering = WeekNumbering.ISO_8601,
            onVisibleDateChanged = {},
            onEventClick = {},
        )
    }
}
