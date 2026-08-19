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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.day.component.AllDayEventsBand
import com.infomaniak.calendar.components.day.component.DayHeader
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.core.common.utils.today
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

/**
 * A single day: its header, the all-day events pinned at the top, and the scrollable hour grid
 * carrying the timed events.
 */
@Composable
fun DayView(
    date: LocalDate,
    events: DayEvents,
    state: DayTimelineState,
    weekNumbering: WeekNumbering,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    headerTrailingContent: @Composable () -> Unit = {},
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md)) {
        DayHeader(
            date = date,
            weekNumbering = weekNumbering,
            trailing = headerTrailingContent,
            modifier = Modifier.padding(horizontal = EsdsTheme.spacing.md),
        )

        AllDayEventsBand(
            events = events.allDay,
            onEventClick = onEventClick,
            modifier = Modifier.padding(horizontal = EsdsTheme.spacing.md),
        )

        DayTimeline(
            date = date,
            events = events,
            state = state,
            onEventClick = onEventClick,
            modifier = Modifier.padding(horizontal = EsdsTheme.spacing.md),
        )
    }
}

@Preview
@Composable
private fun DayViewPreview() {
    Surface {
        DayView(
            date = Clock.today(),
            events = previewDayEvents,
            state = rememberDayTimelineState(scrollState = rememberScrollState(initial = 430)),
            weekNumbering = WeekNumbering.ISO_8601,
            onEventClick = {},
        )
    }
}
