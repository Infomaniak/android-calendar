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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.infomaniak.calendar.components.day.component.MultiDayTimeline
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.model.dateAt
import com.infomaniak.calendar.components.day.model.dayCount
import com.infomaniak.calendar.components.day.model.indexOf
import com.infomaniak.calendar.components.day.paging.FollowSelectedDay
import com.infomaniak.calendar.components.day.paging.pagePositionOf
import com.infomaniak.calendar.components.day.state.DayColumnsState
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventUi
import kotlinx.datetime.LocalDate

/**
 * The days of [dateRange] side by side, turned [daysPerPage] at a time, pages starting from the first
 * day of [dateRange]. Opens on the page of [selectedDate] and follows it from then on, see
 * [FollowSelectedDay] for how moving through the days reports one back through [onVisibleDateChanged].
 *
 * What sets the views apart is only how many days they turn and hold, see [WeekPager] and
 * [ThreeDaysPager].
 */
@Composable
internal fun MultiDayPager(
    dateRange: ClosedRange<LocalDate>,
    daysPerPage: Int,
    selectedDate: () -> LocalDate,
    eventsOf: (LocalDate) -> DayEvents,
    onVisibleDateChanged: (LocalDate) -> Unit,
    onEventClick: (EventUi.Normal) -> Unit,
    state: DayTimelineState,
    columnsState: DayColumnsState,
    modifier: Modifier = Modifier,
    headerModifier: Modifier = Modifier,
    timelineModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val dayCount = dateRange.dayCount
    fun selectedDay() = dateRange.indexOf(selectedDate()).coerceIn(0, dayCount - 1)

    val stripState = rememberLazyListState(
        initialFirstVisibleItemIndex = pagePositionOf(selectedDay(), columnsState.visibleDayCount, daysPerPage)
            .toInt()
            .coerceAtMost(dayCount - columnsState.visibleDayCount.toInt())
            .coerceAtLeast(0),
    )

    FollowSelectedDay(
        stripState = stripState,
        columnsState = columnsState,
        daysPerPage = daysPerPage,
        dayCount = dayCount,
        selectedDay = ::selectedDay,
        onVisibleDayChanged = { onVisibleDateChanged(dateRange.dateAt(it)) },
    )

    MultiDayTimeline(
        dateRange = dateRange,
        stripState = stripState,
        columnsState = columnsState,
        daysPerPage = daysPerPage,
        eventsOf = eventsOf,
        state = state,
        onEventClick = onEventClick,
        modifier = modifier,
        headerModifier = headerModifier,
        timelineModifier = timelineModifier,
        contentPadding = contentPadding,
    )
}
