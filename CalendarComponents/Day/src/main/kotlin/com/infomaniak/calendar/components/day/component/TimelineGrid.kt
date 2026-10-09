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

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.layout.EventLayoutDefaults.DividerHeight
import com.infomaniak.calendar.components.day.model.HOURS_PER_DAY
import com.infomaniak.calendar.components.day.model.MINUTES_PER_HOUR
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatShortTimeLabel
import kotlinx.datetime.LocalTime

private val GridLineThickness = DividerHeight
private const val FIRST_LABELLED_HOUR = 0

private val HourLabelStyle: TextStyle
    @Composable @ReadOnlyComposable get() = MaterialTheme.typography.bodyMedium

/**
 * How far the first and last labels stick out past the grid. Each label is centred on the line it
 * names, so half of midnight's sits above the very first line, where there is no grid left to hold
 * it: the timeline has to keep this much room around the grid, or the label is cut in half by the
 * edge of the screen.
 */
internal val HourLabelOverhang: Dp
    @Composable @ReadOnlyComposable get() = with(LocalDensity.current) { HourLabelStyle.lineHeight.toDp() / 2 }

/**
 * Scrolls the day vertically, with room above for midnight's label and below for the last events to
 * clear whatever overlays the bottom of the screen.
 *
 * A pinch reads its own y as an hour, so any [pinchToZoom][com.infomaniak.calendar.components.day.pinchToZoom]
 * goes after this modifier, inside the padding: it has to start counting where the first hour line
 * is drawn, not where the padding begins.
 *
 * Several of them can share the same [state], as long as they are all as tall: they then scroll as
 * one. Each would stretch on its own on reaching an end though, so [overscrollEffect] is better left
 * out for them.
 */
@Composable
internal fun Modifier.verticalTimelineScroll(
    state: DayTimelineState,
    contentPadding: PaddingValues,
    overscrollEffect: OverscrollEffect? = rememberOverscrollEffect(),
): Modifier = this
    .verticalScroll(state.scrollState, overscrollEffect, enabled = !state.isPinching)
    .padding(
        top = HourLabelOverhang + contentPadding.calculateTopPadding(),
        bottom = DayTimelineDefaults.BottomPadding + contentPadding.calculateBottomPadding(),
    )

/** A line at every hour, across the whole width it is given. */
@Composable
internal fun HourLines(state: DayTimelineState, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .timelineHeight(state)
            .drawBehind {
                for (hour in FIRST_LABELLED_HOUR until HOURS_PER_DAY) {
                    val y = state.verticalOffsetOf(hour * MINUTES_PER_HOUR).toPx()

                    drawLine(
                        color = color,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = GridLineThickness.toPx(),
                    )
                }
            },
    )
}

/**
 * The hour labels alone, each centred on the line it names, across the width it is given: usually
 * [DayTimelineDefaults.HourGutterWidth]. Pair it with [HourLines] sharing the same [state].
 */
@Composable
internal fun HourLabels(state: DayTimelineState, modifier: Modifier = Modifier) {
    Layout(
        modifier = modifier.timelineHeight(state),
        content = { for (hour in FIRST_LABELLED_HOUR until HOURS_PER_DAY) HourLabel(hour) },
    ) { measurables, constraints ->
        val labels = measurables.map { it.measure(Constraints(maxWidth = constraints.maxWidth)) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            labels.forEachIndexed { index, label ->
                val hour = index + FIRST_LABELLED_HOUR
                val hourLineY = state.verticalOffsetOf(hour * MINUTES_PER_HOUR).roundToPx()

                label.place(x = (constraints.maxWidth - label.width) / 2, y = hourLineY - label.height / 2)
            }
        }
    }
}

/** Makes the composable as tall as the whole day at the zoom level of [state]. */
private fun Modifier.timelineHeight(state: DayTimelineState): Modifier = layout { measurable, constraints ->
    val height = state.timelineHeight.roundToPx()
    val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))

    layout(placeable.width, placeable.height) { placeable.place(x = 0, y = 0) }
}

@Composable
private fun HourLabel(hour: Int, modifier: Modifier = Modifier) {
    Text(
        text = LocalTime(hour, 0).formatShortTimeLabel(),
        style = HourLabelStyle,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun TimelineGridPreview() {
    Surface {
        val state = rememberDayTimelineState({})

        Box {
            HourLabels(state = state, modifier = Modifier.width(DayTimelineDefaults.HourGutterWidth))
            HourLines(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = DayTimelineDefaults.HourGutterWidth, end = DayTimelineDefaults.TimelineEndPadding),
            )
        }
    }
}
