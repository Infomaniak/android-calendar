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
package com.infomaniak.calendar.components.day.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.layout.TimedEventsColumn
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.pinchToZoom
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.state.rememberCurrentDateTime
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlin.time.Clock
import kotlinx.datetime.LocalDate

/** A single day's hour grid, carrying its timed events, under the hour gutter's labels. */
@Composable
internal fun DayTimeline(
    date: LocalDate,
    events: DayEvents,
    state: DayTimelineState,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val currentDateTime by rememberCurrentDateTime()
    val gutterWidth = DayTimelineDefaults.HourGutterWidth
    val endPadding = DayTimelineDefaults.TimelineEndPadding

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding.onlyHorizontal())
            .verticalTimelineScroll(state, contentPadding)
            .pinchToZoom(state), // Pinch to zoom after scroll which adds some more paddings
    ) {
        HourLabels(state = state, modifier = Modifier.width(gutterWidth))

        HourLines(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = gutterWidth, end = endPadding),
        )

        TimedEventsColumn(
            events = events.timed,
            hourHeight = state.hourHeight,
            onEventClick = onEventClick,
            modifier = Modifier
                .matchParentSize()
                .padding(start = gutterWidth, end = endPadding),
        )

        if (currentDateTime.date == date) {
            CurrentTimeIndicator(
                minuteOfDay = currentDateTime.minuteOfDay,
                state = state,
                modifier = Modifier
                    .matchParentSize()
                    .padding(start = gutterWidth - EsdsTheme.spacing.md, end = endPadding),
            )
        }
    }
}

@Preview
@Composable
private fun DayTimelinePreview() {
    Surface {
        DayTimeline(
            date = Clock.today(),
            events = previewDayEvents,
            state = rememberDayTimelineState({}, scrollState = rememberScrollState(initial = 430)),
            onEventClick = {},
        )
    }
}

@Preview
@Composable
private fun DayTimelineZoomedOutPreview() {
    Surface {
        DayTimeline(
            date = Clock.today(),
            events = previewDayEvents,
            state = rememberDayTimelineState({}, initialHourHeight = DayTimelineDefaults.MinHourHeight),
            onEventClick = {},
        )
    }
}
