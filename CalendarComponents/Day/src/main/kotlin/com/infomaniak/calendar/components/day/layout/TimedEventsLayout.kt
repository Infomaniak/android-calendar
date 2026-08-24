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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.component.TimedEventCard
import com.infomaniak.calendar.components.day.component.titleSizingFor
import com.infomaniak.calendar.components.day.model.HOURS_PER_DAY
import com.infomaniak.calendar.components.day.model.MINUTES_PER_HOUR
import com.infomaniak.calendar.components.day.model.TimedEvent
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlin.math.roundToInt

@Composable
internal fun TimedEventsLayout(
    timedEvents: List<TimedEvent>,
    placements: List<EventPlacement>,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
) {

    val density = LocalDensity.current
    val currentPlacements = rememberUpdatedState(placements)

    Layout(
        modifier = modifier,
        content = {
            timedEvents.forEachIndexed { index, timedEvent ->
                val placement = placements[index]

                TimedEventCard(
                    timedEvent = timedEvent,
                    titleSizing = titleSizingFor(
                        visibleHeight = with(density) { placement.visibleHeight.toDp() },
                        width = with(density) { placement.width.toDp() },
                    ),
                    visibleHeight = remember(index) { { currentPlacements.value[index].visibleHeight } },
                    onClick = { onEventClick(timedEvent.event) },
                )
            }
        },
    ) { measurables, constraints ->
        val eventsAreaStart = DayTimelineDefaults.HourGutterWidth.roundToPx()

        val cards = measurables.mapIndexed { index, measurable ->
            val placement = placements[index]
            measurable.measure(Constraints.fixed(placement.width.roundToInt(), placement.height.roundToInt()))
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            cards.forEachIndexed { index, card ->
                val placement = placements[index]

                card.place(
                    x = eventsAreaStart + placement.x.roundToInt(),
                    y = placement.y.roundToInt(),
                    zIndex = placement.drawOrder,
                )
            }
        }
    }
}

/**
 * The layout only places what the solver hands it, so the preview has to resolve the overlaps
 * first, over the whole day the way the timeline does.
 *
 * A card sits at the hour it starts, and [previewDayEvents] starts at 7 in the morning, hours below
 * the top of the day: the preview opens scrolled to the first of them, or it would show the empty
 * night.
 */
@Preview
@Composable
private fun TimedEventsLayoutPreview() {
    Surface {
        val density = LocalDensity.current
        val timedEvents = previewDayEvents.timed
        val config = eventLayoutConfig()
        val pixelsPerMinute = with(density) { DayTimelineDefaults.HourHeight.toPx() } / MINUTES_PER_HOUR

        BoxWithConstraints {
            // The layout draws the events past the hour gutter, so it gets the width left after it.
            val eventsAreaEndPadding = DayTimelineDefaults.TimelineEndPadding - EventLayoutDefaults.HorizontalSpacing
            val eventsAreaWidth = maxWidth - DayTimelineDefaults.HourGutterWidth - eventsAreaEndPadding

            val placements = remember(density, config, eventsAreaWidth) {
                with(density) {
                    timedEvents.resolveOverlaps(
                        layoutWidth = eventsAreaWidth.toPx(),
                        pixelsPerMinute = pixelsPerMinute,
                        config = config,
                    )
                }
            }

            val firstEventMinute = timedEvents.minOf { it.startMinuteOfDay }

            Box(
                modifier = Modifier
                    .verticalScroll(rememberScrollState(initial = (firstEventMinute * pixelsPerMinute).roundToInt()))
                    .fillMaxWidth()
                    .height(DayTimelineDefaults.HourHeight * HOURS_PER_DAY),
            ) {
                TimedEventsLayout(
                    timedEvents = timedEvents,
                    placements = placements,
                    onEventClick = {},
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}
