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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.day.component.MultiDayView
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.time.Clock

private const val DAY_COUNT = 3

/** Three days side by side, starting on the selected date. */
@Composable
fun ThreeDayPager(
    selectedDate: () -> LocalDate,
    eventsOf: (LocalDate) -> DayEvents,
    state: DayTimelineState,
    weekNumbering: WeekNumbering,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    headerModifier: Modifier = Modifier,
    timelineModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val firstDay = selectedDate()
    val dates = remember(firstDay) { List(DAY_COUNT) { firstDay.plus(it, DateTimeUnit.DAY) } }

    MultiDayView(
        dates = dates,
        eventsOf = eventsOf,
        state = state,
        weekNumbering = weekNumbering,
        onEventClick = onEventClick,
        headerModifier = headerModifier,
        timelineModifier = timelineModifier,
        contentPadding = contentPadding,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun ThreeDayPagerPreview() {
    Surface {
        ThreeDayPager(
            selectedDate = { Clock.today() },
            eventsOf = { previewDayEvents },
            state = rememberDayTimelineState({}),
            weekNumbering = WeekNumbering.ISO_8601,
            onEventClick = {},
        )
    }
}
