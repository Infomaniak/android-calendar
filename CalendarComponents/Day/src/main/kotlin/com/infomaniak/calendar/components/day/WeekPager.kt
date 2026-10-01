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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.day.component.MultiDayTimeline
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
import kotlinx.datetime.plus
import kotlin.time.Clock

private const val DAYS_PER_PAGE = 7

/**
 * @param columnsState Lets a pinch widen the days, see [rememberDayColumnsState]. Leave it out to
 * keep every day of the page in view.
 */
@Composable
fun WeekPager(
    eventsOf: (LocalDate) -> DayEvents,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    state: DayTimelineState = rememberDayTimelineState(),
    columnsState: DayColumnsState? = null,
    contentPadding: PaddingValues = PaddingValues(),
) {
    // TODO: Page through weeks the way DayPager pages through days.
    val dates = remember { List(DAYS_PER_PAGE) { Clock.today() + DatePeriod(days = it) } }

    MultiDayTimeline(
        dates = dates,
        eventsOf = eventsOf,
        state = state,
        onEventClick = onEventClick,
        modifier = modifier,
        columnsState = columnsState,
        contentPadding = contentPadding,
    )
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            WeekPager(
                eventsOf = { previewDayEvents },
                onEventClick = {},
                columnsState = rememberDayColumnsState(maxVisibleDayCount = DAYS_PER_PAGE))
        }
    }
}
