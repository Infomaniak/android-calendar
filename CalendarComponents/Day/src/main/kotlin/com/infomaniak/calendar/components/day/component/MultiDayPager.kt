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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.layout.EventLayoutDefaults.GridLineThickness
import com.infomaniak.calendar.components.day.layout.TimedEventsColumn
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.pinchToZoom
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.state.rememberCurrentDateTime
import com.infomaniak.calendar.components.foundation.theme.ComponentColors
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.time.Clock

/**
 * Several days side by side, one column each. Each column has a header.
 *
 * Every column follows the same layout as the hour gutter beside them, so a column's header and its timed events all line up with
 * each other.
 */
@Composable
internal fun MultiDayPager(
    dates: List<LocalDate>,
    eventsOf: (LocalDate) -> DayEvents,
    state: DayTimelineState,
    weekNumbering: WeekNumbering,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    headerModifier: Modifier = Modifier,
    timelineModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(0.dp) }
    val contentPadding = contentPadding + PaddingValues(horizontal = EsdsTheme.spacing.md)

    Box(modifier = modifier) {
        MultiDayTimeline(
            dates = dates,
            eventsOf = eventsOf,
            state = state,
            onEventClick = onEventClick,
            contentPadding = contentPadding + PaddingValues(top = headerHeight + EsdsTheme.spacing.md),
            modifier = timelineModifier.fillMaxSize(),
        )

        OverlaidHeaders(
            dates = dates,
            weekNumbering = weekNumbering,
            onSizeChanged = { headerHeight = with(density) { it.height.toDp() } },
            modifier = headerModifier.fillMaxWidth(),
            contentPadding = contentPadding.onlyHorizontalAndTop(),
        )
    }
}

/** The days' hour grid, one column per day, under the hour gutter's labels. */
@Composable
private fun MultiDayTimeline(
    dates: List<LocalDate>,
    eventsOf: (LocalDate) -> DayEvents,
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

        // Each column draws its end vertical divider and the initial vertical divider is drawn inside the gutter slot.
        DayColumns(
            dates = dates,
            modifier = Modifier.matchParentSize(),
            gutter = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    VerticalDivider(
                        thickness = GridLineThickness,
                        color = ComponentColors.GridDividerColor,
                    )
                }
            },
            content = { date ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalDivider(),
                ) {
                    TimedEventsColumn(
                        events = eventsOf(date).timed,
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
            },
        )
    }
}

@Composable
private fun OverlaidHeaders(
    dates: List<LocalDate>,
    weekNumbering: WeekNumbering,
    onSizeChanged: (IntSize) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md),
        modifier = modifier
            .padding(contentPadding)
            // Measured inside the padding: the timeline already makes room for it on its own.
            .onSizeChanged(onSizeChanged),
    ) {
        DayColumns(
            dates = dates,
            gutter = {
                Text(
                    text = stringResource(R.string.weekHeaderWeekNumber, weekNumbering.weekOf(dates.first()).weekNumber),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            content = { date ->
                DayColumnHeader(date, modifier = Modifier.padding(bottom = EsdsTheme.spacing.xl))
            },
            verticalAlignment = Alignment.CenterVertically,
        )
    }
}

/**
 * Creates as many columns as there are [dates]. It takes an extra slot for the [gutter] placed at the start of the columns.
 */
@Composable
private fun DayColumns(
    dates: List<LocalDate>,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    gutter: @Composable () -> Unit = {},
    content: @Composable (LocalDate) -> Unit,
) {
    Row(
        verticalAlignment = verticalAlignment,
        modifier = modifier.padding(end = DayTimelineDefaults.TimelineEndPadding),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.width(DayTimelineDefaults.HourGutterWidth),
        ) {
            gutter()
        }

        dates.forEach { date ->
            DayColumn { content(date) }
        }
    }
}

@Composable
private fun RowScope.DayColumn(content: @Composable () -> Unit) {
    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.weight(1f)) {
        content()
    }
}

/** A vertical line, by default it's drawn at the end of the component */
@Composable
@ReadOnlyComposable
private fun Modifier.verticalDivider(
    position: DayColumnDividerPosition? = null,
    color: Color = ComponentColors.GridDividerColor,
): Modifier = drawBehind {
    val x = when (position) {
        DayColumnDividerPosition.Start -> 0f
        DayColumnDividerPosition.End -> size.width
        null -> if (layoutDirection == LayoutDirection.Ltr) size.width else 0f
    }
    drawLine(
        color = color,
        start = Offset(x, 0f),
        end = Offset(x, size.height),
        strokeWidth = GridLineThickness.toPx(),
    )
}
@Composable
private fun PaddingValues.onlyHorizontalAndTop(): PaddingValues = onlyHorizontal() + PaddingValues(top = calculateTopPadding())

internal enum class DayColumnDividerPosition {
    Start, End
}

private val previewDates = Clock.today().let { today -> List(3) { today.plus(it, DateTimeUnit.DAY) } }

@Preview
@Composable
private fun MultiDayPagerPreview() {
    Surface {
        MultiDayPager(
            dates = previewDates,
            eventsOf = { previewDayEvents },
            state = rememberDayTimelineState({}, scrollState = rememberScrollState(initial = 430)),
            weekNumbering = WeekNumbering.ISO_8601,
            headerModifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            onEventClick = {},
        )
    }
}
