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

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.layout.TimedEventsColumn
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayColumnsState
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayColumnsState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.day.zoom.pinchToZoom
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.state.rememberCurrentDateTime
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.time.Clock

/**
 * Several days side by side, each in its own column of the hour grid, under a single hour gutter.
 *
 * @param columnsState Lets a pinch widen the columns, scrolling the days that no longer fit. Without
 * it, the [dates] always share the width evenly and a pinch only zooms the hours.
 */
@Composable
internal fun MultiDayTimeline(
    dates: List<LocalDate>,
    eventsOf: (LocalDate) -> DayEvents,
    state: DayTimelineState,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    columnsState: DayColumnsState? = null,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val gutterWidth = DayTimelineDefaults.HourGutterWidth
    val endPadding = DayTimelineDefaults.TimelineEndPadding

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding.onlyHorizontal()),
    ) {
        val viewportWidth = maxWidth - gutterWidth - endPadding
        val columnWidth = columnsState?.columnWidth(viewportWidth) ?: (viewportWidth / dates.size)

        Row(modifier = Modifier.verticalTimelineScroll(state, contentPadding)) {
            HourLabels(state = state, modifier = Modifier.width(gutterWidth))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .dayColumnsScroll(columnsState)
                    .pinchToZoom(horizontal = columnsState?.zoomAxis, vertical = state.zoomAxis),
            ) {
                DayColumns(
                    dates = dates,
                    eventsOf = eventsOf,
                    columnWidth = columnWidth,
                    state = state,
                    onEventClick = onEventClick,
                    modifier = Modifier.padding(end = endPadding),
                )
            }
        }
    }
}

private fun Modifier.dayColumnsScroll(columnsState: DayColumnsState?): Modifier {
    if (columnsState == null) return this
    return horizontalScroll(columnsState.scrollState, enabled = !columnsState.isPinching)
}

/**
 * Sized explicitly rather than filling its parent: under a horizontal scroll, there is no width
 * left to fill.
 */
@Composable
private fun DayColumns(
    dates: List<LocalDate>,
    eventsOf: (LocalDate) -> DayEvents,
    columnWidth: Dp,
    state: DayTimelineState,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentDateTime by rememberCurrentDateTime()

    Box(modifier = modifier.width(columnWidth * dates.size)) {
        HourLines(state = state, modifier = Modifier.fillMaxWidth())
        DayColumnDividers(columnWidth = columnWidth, columnCount = dates.size, modifier = Modifier.matchParentSize())

        Row(modifier = Modifier.matchParentSize()) {
            dates.forEach { date ->
                key(date) {
                    Box(
                        modifier = Modifier
                            .width(columnWidth)
                            .fillMaxHeight(),
                    ) {
                        TimedEventsColumn(
                            events = eventsOf(date).timed,
                            layoutWidth = columnWidth,
                            hourHeight = state.hourHeight,
                            onEventClick = onEventClick,
                            modifier = Modifier.matchParentSize(),
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
            }
        }
    }
}

private val previewDates = List(3) { Clock.today() + DatePeriod(days = it) }

@Preview
@Composable
private fun MultiDayTimelinePreview() {
    Surface {
        MultiDayTimeline(
            dates = previewDates,
            eventsOf = { previewDayEvents },
            state = rememberDayTimelineState(scrollState = rememberScrollState(initial = 430)),
            onEventClick = {},
        )
    }
}

@Preview
@Composable
private fun MultiDayTimelineZoomedInPreview() {
    Surface {
        MultiDayTimeline(
            dates = previewDates,
            eventsOf = { previewDayEvents },
            state = rememberDayTimelineState(scrollState = rememberScrollState(initial = 430)),
            columnsState = rememberDayColumnsState(maxVisibleDayCount = 3).apply { zoomAxis.zoom = 2f },
            onEventClick = {},
        )
    }
}
