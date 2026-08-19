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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.day.component.CurrentTimeIndicator
import com.infomaniak.calendar.components.day.component.HourGrid
import com.infomaniak.calendar.components.day.component.HourLabelOverhang
import com.infomaniak.calendar.components.day.layout.EventLayoutDefaults
import com.infomaniak.calendar.components.day.layout.TimedEventsLayout
import com.infomaniak.calendar.components.day.layout.eventLayoutConfig
import com.infomaniak.calendar.components.day.layout.resolveOverlaps
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.model.MINUTES_PER_HOUR
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.minuteOfDay
import com.infomaniak.calendar.components.day.state.rememberCurrentDateTime
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

/**
 * Overlaps are resolved once per width and zoom level rather than on every frame, since the
 * arrangement only changes when one of the two does.
 */
@Composable
fun DayTimeline(
    date: LocalDate,
    events: DayEvents,
    state: DayTimelineState,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentDateTime by rememberCurrentDateTime()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val config = eventLayoutConfig()
        // The solver strips horizontalSpacing off the end of every card, so the area runs that far
        // past the timeline's end padding for the last column of cards to stop exactly on it.
        val eventsAreaEndPadding = DayTimelineDefaults.TimelineEndPadding - EventLayoutDefaults.HorizontalSpacing
        val eventsAreaWidth = maxWidth - DayTimelineDefaults.HourGutterWidth - eventsAreaEndPadding

        val placements = remember(events, eventsAreaWidth, state.hourHeight, config) {
            with(density) {
                events.timed.resolveOverlaps(
                    layoutWidth = eventsAreaWidth.toPx(),
                    pixelsPerMinute = state.hourHeight.toPx() / MINUTES_PER_HOUR,
                    config = config,
                )
            }
        }

        // The timeline runs under the navigation bar, and the toolbar floats above it: the end of
        // the day has to clear the two of them stacked.
        val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Box(
            modifier = Modifier
                .verticalScroll(state.scrollState, enabled = !state.isPinching)
                .pinchToZoom(state)
                .padding(top = HourLabelOverhang, bottom = DayTimelineDefaults.BottomPadding + navigationBarPadding),
        ) {
            HourGrid(state = state, modifier = Modifier.fillMaxWidth())

            TimedEventsLayout(
                timedEvents = events.timed,
                placements = placements,
                onEventClick = onEventClick,
                modifier = Modifier.matchParentSize(),
            )

            if (currentDateTime.date == date) {
                CurrentTimeIndicator(
                    minuteOfDay = currentDateTime.minuteOfDay, state = state, modifier = Modifier.matchParentSize(),
                )
            }
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
            state = rememberDayTimelineState(scrollState = rememberScrollState(initial = 430)),
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
            state = rememberDayTimelineState(initialHourHeight = DayTimelineDefaults.MinHourHeight),
            onEventClick = {},
        )
    }
}
