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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayColumnsState
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayColumnsState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Clock

private const val VISIBLE_DAYS = 3
private const val DAYS_PER_PAGE = 1

/**
 * The days of [dateRange], 3 at a time, stepped through one day at a time.
 *
 * Opens with [selectedDate] as its first day, and follows it from then on: see [MultiDayPager].
 *
 * @param columnsState How many days the viewport holds. By default, it holds 3 days and a pinch only
 * zooms the hours: see [rememberDayColumnsState] to let a pinch widen the days as well.
 * @param headerModifier Applied behind the header at the top of each day, which the grid scrolls under.
 * @param timelineModifier Applied to the grid scrolling under the headers.
 */
@Composable
fun ThreeDaysPager(
    dateRange: ClosedRange<LocalDate>,
    selectedDate: () -> LocalDate,
    eventsOf: (LocalDate) -> DayEvents,
    onVisibleDateChanged: (LocalDate) -> Unit,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    headerModifier: Modifier = Modifier,
    timelineModifier: Modifier = Modifier,
    state: DayTimelineState = rememberDayTimelineState(),
    columnsState: DayColumnsState = rememberDayColumnsState(
        maxVisibleDayCount = VISIBLE_DAYS,
        minVisibleDayCount = VISIBLE_DAYS,
    ),
    contentPadding: PaddingValues = PaddingValues(),
) {
    MultiDayPager(
        dateRange = dateRange,
        daysPerPage = DAYS_PER_PAGE,
        selectedDate = selectedDate,
        eventsOf = eventsOf,
        onVisibleDateChanged = onVisibleDateChanged,
        onEventClick = onEventClick,
        state = state,
        columnsState = columnsState,
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
            ThreeDaysPager(
                dateRange = today.minus(DatePeriod(days = 14))..today.plus(DatePeriod(days = 14)),
                selectedDate = { today },
                eventsOf = { previewDayEvents },
                onVisibleDateChanged = {},
                onEventClick = {},
            )
        }
    }
}
