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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.layout.TimedEventsColumn
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.model.MINUTES_PER_HOUR
import com.infomaniak.calendar.components.day.pinchToZoom
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.state.rememberCurrentDateTime
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

private val LocalDateTime.minuteOfDay: Int get() = hour * MINUTES_PER_HOUR + minute

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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding.onlyHorizontal())
            .verticalScroll(state.scrollState, enabled = !state.isPinching)
            .padding(
                top = HourLabelOverhang + contentPadding.calculateTopPadding(),
                bottom = DayTimelineDefaults.BottomPadding + contentPadding.calculateBottomPadding(),
            )
            // Inside the padding: a pinch reads its own y as an hour, so it has to start
            // counting where the first hour line is drawn, not where the padding begins.
            .pinchToZoom(state),
    ) {
        HourLabels(state = state, modifier = Modifier.width(DayTimelineDefaults.HourGutterWidth))
        HourLines(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = DayTimelineDefaults.HourGutterWidth, end = DayTimelineDefaults.TimelineEndPadding),
        )

        TimedEventsColumn(
            events = events.timed,
            hourHeight = state.hourHeight,
            onEventClick = onEventClick,
            modifier = Modifier
                .matchParentSize()
                .padding(start = DayTimelineDefaults.HourGutterWidth, end = DayTimelineDefaults.TimelineEndPadding),
        )

        if (currentDateTime.date == date) {
            CurrentTimeIndicator(
                minuteOfDay = currentDateTime.minuteOfDay,
                state = state,
                modifier = Modifier.matchParentSize(),
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
