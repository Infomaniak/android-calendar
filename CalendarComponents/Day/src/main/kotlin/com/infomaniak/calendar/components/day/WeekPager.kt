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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.day.component.MultiDayTimeline
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.model.dateAt
import com.infomaniak.calendar.components.day.model.dayCount
import com.infomaniak.calendar.components.day.model.indexOf
import com.infomaniak.calendar.components.day.paging.FollowSelectedDay
import com.infomaniak.calendar.components.day.paging.pagePositionOf
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayColumnsState
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayColumnsState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.utils.startOfWeek
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinDayOfWeek
import kotlin.time.Clock

private const val DAYS_PER_WEEK = 7

/**
 * The weeks of [dateRange], turned one at a time. Zoomed in, the week in view scrolls to its end
 * before the next one turns.
 *
 * Opens on the week of [selectedDate], and follows it from then on: see [FollowSelectedDay] for how
 * moving through the weeks reports a day back through [onVisibleDateChanged].
 *
 * @param dateRange Widened to whole weeks, as [weekNumbering] starts them.
 * @param columnsState How many days the viewport holds. By default, it holds the whole week and a
 * pinch only zooms the hours: see [rememberDayColumnsState] to let a pinch widen the days as well.
 * @param headerModifier Applied behind the header at the top of each day, which the grid scrolls under.
 * @param timelineModifier Applied to the grid scrolling under the headers.
 */
@Composable
fun WeekPager(
    dateRange: ClosedRange<LocalDate>,
    selectedDate: () -> LocalDate,
    weekNumbering: WeekNumbering,
    eventsOf: (LocalDate) -> DayEvents,
    onVisibleDateChanged: (LocalDate) -> Unit,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    headerModifier: Modifier = Modifier,
    timelineModifier: Modifier = Modifier,
    state: DayTimelineState = rememberDayTimelineState(),
    columnsState: DayColumnsState = rememberDayColumnsState(
        maxVisibleDayCount = DAYS_PER_WEEK,
        minVisibleDayCount = DAYS_PER_WEEK,
    ),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val firstDayOfWeek = remember(weekNumbering) { weekNumbering.firstDayOfWeek.toKotlinDayOfWeek() }
    val weeksRange = remember(dateRange, firstDayOfWeek) {
        dateRange.start.startOfWeek(firstDayOfWeek)..dateRange.endInclusive.startOfWeek(firstDayOfWeek)
            .plus(DAYS_PER_WEEK - 1, DateTimeUnit.DAY)
    }
    val dayCount = weeksRange.dayCount
    fun selectedDay() = weeksRange.indexOf(selectedDate()).coerceIn(0, dayCount - 1)

    val stripState = rememberLazyListState(
        initialFirstVisibleItemIndex = pagePositionOf(selectedDay(), columnsState.visibleDayCount, DAYS_PER_WEEK).toInt(),
    )

    FollowSelectedDay(
        stripState = stripState,
        columnsState = columnsState,
        daysPerPage = DAYS_PER_WEEK,
        dayCount = dayCount,
        selectedDay = ::selectedDay,
        onVisibleDayChanged = { onVisibleDateChanged(weeksRange.dateAt(it)) },
    )

    MultiDayTimeline(
        dateRange = weeksRange,
        stripState = stripState,
        columnsState = columnsState,
        daysPerPage = DAYS_PER_WEEK,
        eventsOf = eventsOf,
        state = state,
        onEventClick = onEventClick,
        modifier = modifier,
        headerModifier = headerModifier,
        timelineModifier = timelineModifier,
        contentPadding = contentPadding,
    )
}

@Preview
@Composable
private fun Preview() {
    val today = Clock.today()

    MaterialTheme {
        Surface {
            WeekPager(
                dateRange = today.minus(DatePeriod(days = 14))..today.plus(DatePeriod(days = 14)),
                selectedDate = { today },
                weekNumbering = WeekNumbering.ISO_8601,
                eventsOf = { previewDayEvents },
                onVisibleDateChanged = {},
                onEventClick = {},
                columnsState = rememberDayColumnsState(maxVisibleDayCount = DAYS_PER_WEEK),
            )
        }
    }
}
