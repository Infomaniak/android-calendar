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
package com.infomaniak.calendar.components.day.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.model.HOURS_PER_DAY
import com.infomaniak.calendar.components.day.model.MINUTES_PER_HOUR
import com.infomaniak.calendar.components.day.model.TimedEvent
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.foundation.models.EventUi
import kotlin.math.roundToInt

/**
 * One day's timed events, arranged side by side where they overlap.
 *
 * Overlaps are resolved once per width and zoom level rather than on every frame, since the
 * arrangement only changes when one of the two does.
 *
 * @param layoutWidth The width the solver shares between concurrent events. It may run past the
 * composable's own bounds, since the solver strips [EventLayoutDefaults.HorizontalSpacing] off the
 * end of every card.
 */
@Composable
internal fun TimedEventsColumn(
    events: List<TimedEvent>,
    layoutWidth: Dp,
    hourHeight: Dp,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val config = eventLayoutConfig()

    val placements = remember(events, layoutWidth, hourHeight, config, density) {
        with(density) {
            events.resolveOverlaps(
                layoutWidth = layoutWidth.toPx(),
                pixelsPerMinute = hourHeight.toPx() / MINUTES_PER_HOUR,
                config = config,
            )
        }
    }

    ResizableEventLayout(
        timedEvents = events,
        placements = placements,
        onEventClick = onEventClick,
        modifier = modifier,
    )
}

/**
 * A card sits at the hour it starts, and [previewDayEvents] starts at 7 in the morning, hours below
 * the top of the day: the preview opens scrolled to the first of them, or it would show the empty
 * night.
 */
@Preview
@Composable
private fun TimedEventsColumnPreview() {
    Surface {
        val timedEvents = previewDayEvents.timed
        val hourHeight = DayTimelineDefaults.HourHeight
        val firstEventOffset = with(LocalDensity.current) {
            (hourHeight * timedEvents.minOf { it.startMinuteOfDay } / MINUTES_PER_HOUR).toPx()
        }

        BoxWithConstraints {
            val layoutWidth = maxWidth

            Box(
                modifier = Modifier
                    .verticalScroll(rememberScrollState(initial = firstEventOffset.roundToInt()))
                    .fillMaxWidth()
                    .height(hourHeight * HOURS_PER_DAY),
            ) {
                TimedEventsColumn(
                    events = timedEvents,
                    layoutWidth = layoutWidth,
                    hourHeight = hourHeight,
                    onEventClick = {},
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}
