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
package com.infomaniak.calendar.components.month.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.infomaniak.calendar.components.event.highlight.DraggedCardPlacement
import com.infomaniak.calendar.components.event.highlight.EventHighlightState
import com.infomaniak.calendar.components.event.highlight.eventDropTarget
import com.infomaniak.calendar.components.event.highlight.highlightable
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.components.foundation.utils.HorizontalGridLine
import com.infomaniak.calendar.components.month.layout.LaneMetrics
import com.infomaniak.calendar.components.month.layout.MonthDefaults
import com.infomaniak.calendar.components.month.layout.laneMetrics
import com.infomaniak.calendar.components.month.layout.visibleEventsIn
import com.infomaniak.calendar.components.month.model.MonthWeekUi
import com.infomaniak.calendar.components.month.preview.previewFiveWeeksMonth
import com.infomaniak.calendar.components.month.preview.mockPreviewMonth
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.onDay
import kotlinx.datetime.yearMonth
import kotlin.math.roundToInt

/**
 * A week of the month grid: its days, [selectedDate] marked, with the events of the week over them in [laneCount] lanes.
 * Taps between the events reach the days below.
 */
@Composable
internal fun MonthWeekRow(
    week: MonthWeekUi,
    month: YearMonth,
    selectedDate: () -> LocalDate,
    today: LocalDate,
    laneMetrics: LaneMetrics,
    laneCount: Int,
    onDayClick: (LocalDate) -> Unit,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    highlightState: EventHighlightState? = null,
) {
    Box(modifier) {
        Row(Modifier.fillMaxSize()) {
            week.dates.forEachIndexed { column, date ->
                MonthDayCell(
                    date = date,
                    isInMonth = date.yearMonth == month,
                    isSelected = { date == selectedDate() },
                    isToday = date == today,
                    eventCount = week.eventCountOf(column),
                    onClick = { onDayClick(date) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(highlightState?.let { Modifier.eventDropTarget(it, date) } ?: Modifier),
                )
            }
        }

        HorizontalGridLine()

        if (laneCount > 0) WeekEvents(week, laneMetrics, laneCount, onEventClick, highlightState, Modifier.matchParentSize())
    }
}

/** Places each visible event over the days it covers, on its lane, and the overflow events indicators in the last lane. */
@Composable
private fun WeekEvents(
    week: MonthWeekUi,
    laneMetrics: LaneMetrics,
    laneCount: Int,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    highlightState: EventHighlightState?,
    modifier: Modifier = Modifier,
) {
    val visibleEvents = remember(week, laneCount) { week.visibleEventsIn(laneCount) }
    val horizontalInset = MonthDefaults.EventHorizontalInset
    val chipInset = with(LocalDensity.current) { horizontalInset.toPx() }

    Layout(
        content = {
            visibleEvents.segments.forEach { segment ->
                key(segment.event.id, segment.startColumn) {
                    MonthEventChip(
                        event = segment.event,
                        onClick = { onEventClick(segment.event) },
                        modifier = Modifier
                            .layoutId(EventSlot(segment.startColumn, segment.span, segment.lane))
                            // An event over several weeks has a segment in each, alike but for their week.
                            .then(
                                highlightState?.let {
                                    Modifier.highlightable(
                                        state = it,
                                        event = segment.event,
                                        key = week.dates.first() to segment,
                                        draggedCardPlacement = firstDayUnderFinger(segment.span, chipInset),
                                    ) { draggedModifier ->
                                        MonthEventChip(event = segment.event, onClick = {}, modifier = draggedModifier)
                                    }
                                } ?: Modifier,
                            ),
                    )
                }
            }
            visibleEvents.hasHiddenEvents.forEachIndexed { column, hasHiddenEvents ->
                if (hasHiddenEvents) {
                    key(column) {
                        val slot = EventSlot(column, span = 1, lane = visibleEvents.overflowEventsIndicatorLane)
                        OverflowEventsIndicator(Modifier.layoutId(slot))
                    }
                }
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val columnWidth = constraints.maxWidth / week.dates.size.toFloat()
        val insetPx = horizontalInset.roundToPx()

        val placeables = measurables.map { measurable ->
            val slot = measurable.layoutId as EventSlot
            val start = (slot.startColumn * columnWidth).roundToInt() + insetPx
            val end = ((slot.startColumn + slot.span) * columnWidth).roundToInt() - insetPx
            val placeable = measurable.measure(Constraints.fixed((end - start).coerceAtLeast(0), laneMetrics.laneHeight))
            placeable to IntOffset(start, laneMetrics.laneTop(slot.lane))
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { (placeable, position) -> placeable.placeRelative(position) }
        }
    }
}

/**
 * Holds a dragged chip [span] days long by its first day, under the finger as far into it as the finger was into
 * the day it grabbed, so wherever the chip is grabbed, its first day is the one it is dropped on.
 */
private fun firstDayUnderFinger(span: Int, inset: Float) = DraggedCardPlacement { card, grab, layoutDirection ->
    val columnWidth = (card.width + 2 * inset) / span
    val fromStart = if (layoutDirection == LayoutDirection.Ltr) grab.x else card.width - grab.x
    val startEdge = inset - (fromStart + inset) % columnWidth
    val left = if (layoutDirection == LayoutDirection.Ltr) startEdge else -startEdge - card.width
    Rect(left = left, top = -grab.y, right = left + card.width, bottom = card.height - grab.y)
}

/** How many events cover the day of [column], hidden ones included. */
private fun MonthWeekUi.eventCountOf(column: Int): Int = segments.count { column in it.columns }

@Preview
@Composable
private fun Preview() {
    PreviewWeekRow(weekIndex = 1, laneCount = 3)
}

@Preview(name = "Without events")
@Composable
private fun PreviewWithoutEvents() {
    PreviewWeekRow(weekIndex = 0, laneCount = 3)
}

@Preview(name = "Right to left")
@Composable
private fun PreviewRtl() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        PreviewWeekRow(weekIndex = 1, laneCount = 4)
    }
}

@Composable
private fun PreviewWeekRow(weekIndex: Int, laneCount: Int) {
    val month = mockPreviewMonth(previewFiveWeeksMonth)

    MaterialTheme {
        Surface {
            val laneMetrics = laneMetrics()
            MonthWeekRow(
                week = month.weeks[weekIndex],
                month = month.month,
                selectedDate = { month.month.onDay(8) },
                today = month.month.onDay(6),
                laneMetrics = laneMetrics,
                laneCount = laneCount,
                onDayClick = {},
                onEventClick = {},
                modifier = Modifier.height(with(LocalDensity.current) { laneMetrics.rowHeightFor(laneCount).toDp() }),
            )
        }
    }
}

/** Where a child of [WeekEvents] goes: over [span] columns from [startColumn], on [lane]. */
private data class EventSlot(val startColumn: Int, val span: Int, val lane: Int)
