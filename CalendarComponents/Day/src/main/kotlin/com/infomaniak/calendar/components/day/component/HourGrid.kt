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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.model.HOURS_PER_DAY
import com.infomaniak.calendar.components.day.model.MINUTES_PER_HOUR
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatShortTimeLabel
import kotlinx.datetime.LocalTime

private val HourLineWidth = 1.dp
private const val FIRST_LABELLED_HOUR = 0

private val HourLabelStyle: TextStyle
    @Composable get() = MaterialTheme.typography.bodyMedium

/**
 * How far the first and last labels stick out past the grid. Each label is centred on the line it
 * names, so half of midnight's sits above the very first line, where there is no grid left to hold
 * it: the timeline has to keep this much room around the grid, or the label is cut in half by the
 * edge of the screen.
 */
internal val HourLabelOverhang: Dp
    @Composable get() = with(LocalDensity.current) { HourLabelStyle.lineHeight.toDp() / 2 }

@Composable
internal fun HourGrid(state: DayTimelineState, modifier: Modifier = Modifier) {
    val hourLineColor = MaterialTheme.colorScheme.outlineVariant
    val hourLineEndPadding = DayTimelineDefaults.TimelineEndPadding

    Box(
        modifier = modifier
            .timelineHeight(state)
            .drawBehind { drawHourLines(state, hourLineColor, hourLineEndPadding) },
    ) {
        HourLabels(
            state = state,
            modifier = Modifier
                .width(DayTimelineDefaults.HourGutterWidth)
                .fillMaxHeight(),
        )
    }
}

private fun Modifier.timelineHeight(state: DayTimelineState): Modifier = layout { measurable, constraints ->
    val height = state.timelineHeight.roundToPx()
    val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))

    layout(placeable.width, placeable.height) { placeable.place(x = 0, y = 0) }
}

private fun DrawScope.drawHourLines(state: DayTimelineState, color: Color, endPadding: Dp) {
    val lineStartX = DayTimelineDefaults.HourGutterWidth.toPx()

    for (hour in FIRST_LABELLED_HOUR until HOURS_PER_DAY) {
        val y = state.verticalOffsetOf(hour * MINUTES_PER_HOUR).toPx()

        drawLine(
            color = color,
            start = Offset(lineStartX, y),
            end = Offset(size.width - endPadding.toPx(), y),
            strokeWidth = HourLineWidth.toPx(),
        )
    }
}

@Composable
private fun HourLabels(state: DayTimelineState, modifier: Modifier = Modifier) {
    Layout(
        modifier = modifier,
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
private fun HourGridPreview() {
    Surface {
        HourGrid(state = rememberDayTimelineState(), modifier = Modifier.fillMaxWidth())
    }
}
