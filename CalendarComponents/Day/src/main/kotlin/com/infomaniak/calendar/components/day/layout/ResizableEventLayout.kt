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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import com.infomaniak.calendar.components.day.TimelineDefaults
import com.infomaniak.calendar.components.day.component.ResizableEventItem
import com.infomaniak.calendar.components.day.component.titleSizingFor
import com.infomaniak.calendar.components.day.model.HOURS_PER_DAY
import com.infomaniak.calendar.components.day.model.MINUTES_PER_HOUR
import com.infomaniak.calendar.components.day.model.TimedEvent
import com.infomaniak.calendar.components.day.model.firstDayOfEvent
import com.infomaniak.calendar.components.day.model.isOverSeveralDays
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.event.highlight.DraggedCardPlacement
import com.infomaniak.calendar.components.event.highlight.EventHighlightState
import com.infomaniak.calendar.components.event.highlight.highlightable
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import kotlin.math.roundToInt

/**
 * Places every card where the solver put it, relative to its own start edge.
 *
 * A card dragged from one day of an event running over several is drawn as the event's first day instead, its
 * start under the finger, [pixelsPerMinute] telling how tall that day is.
 */
@Composable
internal fun ResizableEventLayout(
    timedEvents: List<TimedEvent>,
    placements: List<EventPlacement>,
    pixelsPerMinute: Float,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    textOverflow: TextOverflow = TextOverflow.Ellipsis,
    highlightState: EventHighlightState? = null,
) {
    val density = LocalDensity.current
    val config = eventLayoutConfig()
    val currentPlacements = rememberUpdatedState(placements)

    Layout(
        modifier = modifier,
        content = {
            timedEvents.forEachIndexed { index, timedEvent ->
                val placement = placements[index]
                val titleSizing = titleSizingFor(
                    visibleHeight = with(density) { placement.visibleHeight.toDp() },
                    width = with(density) { placement.width.toDp() },
                )
                val visibleHeight = remember(index) { { currentPlacements.value[index].visibleHeight } }

                ResizableEventItem(
                    timedEvent = timedEvent,
                    titleSizing = titleSizing,
                    visibleHeight = visibleHeight,
                    onClick = { onEventClick(timedEvent.event) },
                    textOverflow = textOverflow,
                    modifier = highlightState?.let {
                        val firstDay = timedEvent.firstDayOfEvent()
                        val isOverSeveralDays = timedEvent.event.isOverSeveralDays
                        val draggedHeight = (firstDay.durationMinutes * pixelsPerMinute - config.verticalSpacing)
                            .coerceAtLeast(config.minEventHeight)

                        Modifier.highlightable(
                            state = it,
                            event = timedEvent.event,
                            key = timedEvent,
                            draggedCardPlacement = if (isOverSeveralDays) {
                                DraggedCardPlacement { card, grab, _ ->
                                    Rect(left = -grab.x, top = 0f, right = card.width - grab.x, bottom = draggedHeight)
                                }
                            } else {
                                DraggedCardPlacement.AsGrabbed
                            },
                        ) { draggedModifier ->
                            // Dropped, the card goes back to this piece.
                            val showsFirstDay = isOverSeveralDays && it.isDragging
                            ResizableEventItem(
                                timedEvent = if (showsFirstDay) firstDay else timedEvent,
                                titleSizing = if (showsFirstDay) {
                                    titleSizingFor(
                                        visibleHeight = with(density) { draggedHeight.toDp() },
                                        width = with(density) { placement.width.toDp() },
                                    )
                                } else {
                                    titleSizing
                                },
                                visibleHeight = if (showsFirstDay) ({ draggedHeight }) else visibleHeight,
                                onClick = {},
                                textOverflow = textOverflow,
                                modifier = draggedModifier,
                            )
                        }
                    } ?: Modifier,
                )
            }
        },
    ) { measurables, constraints ->
        val cards = measurables.mapIndexed { index, measurable ->
            val placement = placements[index]
            measurable.measure(Constraints.fixed(placement.width.roundToInt(), placement.height.roundToInt()))
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            cards.forEachIndexed { index, card ->
                val placement = placements[index]

                card.place(
                    x = placement.x.roundToInt(),
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
 * the top of the day: the preview opens scrolled to the first of them, or it would show the empty night.
 */
@Preview
@Composable
private fun ResizableEventLayoutPreview() {
    Surface {
        val density = LocalDensity.current
        val timedEvents = previewDayEvents.timed
        val config = eventLayoutConfig()
        val pixelsPerMinute = with(density) { TimelineDefaults.HourHeight.toPx() } / MINUTES_PER_HOUR

        BoxWithConstraints {
            val placements = remember(density, config, constraints.maxWidth) {
                timedEvents.resolveOverlaps(
                    layoutWidth = constraints.maxWidth.toFloat(),
                    pixelsPerMinute = pixelsPerMinute,
                    config = config,
                )
            }

            val firstEventMinute = timedEvents.minOf { it.startMinuteOfDay }

            Box(
                modifier = Modifier
                    .verticalScroll(rememberScrollState(initial = (firstEventMinute * pixelsPerMinute).roundToInt()))
                    .fillMaxWidth()
                    .height(TimelineDefaults.HourHeight * HOURS_PER_DAY),
            ) {
                ResizableEventLayout(
                    timedEvents = timedEvents,
                    placements = placements,
                    pixelsPerMinute = pixelsPerMinute,
                    onEventClick = {},
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}
