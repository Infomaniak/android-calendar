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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.components.foundation.state.rememberToday
import com.infomaniak.calendar.components.foundation.utils.horizontalBottomDivider
import com.infomaniak.calendar.components.foundation.utils.onlyHorizontalAndTop
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatShortDayNameUppercase
import com.infomaniak.calendar.components.foundation.utils.verticalStartDivider
import com.infomaniak.calendar.components.month.layout.laneMetrics
import com.infomaniak.calendar.components.month.layout.monthRowHeights
import com.infomaniak.calendar.components.month.model.MonthUi
import com.infomaniak.calendar.components.month.preview.previewFiveWeeksMonth
import com.infomaniak.calendar.components.month.preview.mockPreviewMonth
import com.infomaniak.calendar.components.month.preview.previewSixWeeksMonth
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.onDay

/**
 * A month: the weekday header over its weeks, [selectedDate] marked. The rows share the page's height above the bottom of
 * [contentPadding], unless that leaves them too short, where they scroll instead, that padding staying below the last one.
 */
@Composable
internal fun MonthPage(
    month: MonthUi,
    selectedDate: () -> LocalDate,
    onDayClick: (LocalDate) -> Unit,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    today: LocalDate = rememberToday().value,
) {
    val layoutDirection = LocalLayoutDirection.current
    // We need to know if two pages are touching or if they are separated because some system padding is present. This is used to
    // avoid drawing two vertical consecutive divider.
    val touchesPreviousGrid = contentPadding.calculateStartPadding(layoutDirection) == 0.dp &&
            contentPadding.calculateEndPadding(layoutDirection) == 0.dp

    Column(modifier.padding(contentPadding.onlyHorizontalAndTop())) {
        WeekdayHeader(month.weeks.first().dates)
        WeekRows(
            month = month,
            selectedDate = selectedDate,
            today = today,
            bottomPadding = contentPadding.calculateBottomPadding(),
            touchesPreviousGrid = touchesPreviousGrid,
            onDayClick = onDayClick,
            onEventClick = onEventClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun WeekdayHeader(dates: List<LocalDate>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = EsdsTheme.spacing.xl)
            .semantics(mergeDescendants = true) { heading() },
    ) {
        dates.forEach { date ->
            Text(
                text = date.formatShortDayNameUppercase(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun WeekRows(
    month: MonthUi,
    selectedDate: () -> LocalDate,
    today: LocalDate,
    bottomPadding: Dp,
    touchesPreviousGrid: Boolean,
    onDayClick: (LocalDate) -> Unit,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    BoxWithConstraints(modifier) {
        val laneMetrics = laneMetrics()
        val rowsSize = IntSize(constraints.maxWidth, constraints.maxHeight - with(density) { bottomPadding.roundToPx() })
        val rowHeights = monthRowHeights(rowsSize, month.weeks, laneMetrics)
        val laneCount = laneMetrics.laneCount(rowHeights.height)

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomPadding)
                .outerGridEdges(touchesPreviousGrid),
        ) {
            month.weeks.forEachIndexed { index, week ->
                val height = if (index == month.weeks.lastIndex) rowHeights.lastHeight else rowHeights.height
                MonthWeekRow(
                    week = week,
                    month = month.month,
                    selectedDate = selectedDate,
                    today = today,
                    laneMetrics = laneMetrics,
                    laneCount = laneCount,
                    onDayClick = onDayClick,
                    onEventClick = onEventClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { height.toDp() }),
                )
            }
        }
    }
}

/**
 * Closes the grid, whose rows and days only draw their top and end lines: along its bottom, and along its start unless
 * it [touchesPreviousGrid], whose last days' end line already runs there and would be doubled.
 */
@Composable
private fun Modifier.outerGridEdges(touchesPreviousGrid: Boolean): Modifier {
    return horizontalBottomDivider().then(if (touchesPreviousGrid) Modifier else Modifier.verticalStartDivider())
}

@Preview
@Composable
private fun Preview() {
    PreviewMonthPage(mockPreviewMonth(previewFiveWeeksMonth), contentPadding = PaddingValues(bottom = 80.dp))
}

@Preview(name = "Inset from the end")
@Composable
private fun PreviewInsetFromEnd() {
    PreviewMonthPage(mockPreviewMonth(previewFiveWeeksMonth), contentPadding = PaddingValues(end = 24.dp))
}

@Preview(name = "Six weeks")
@Composable
private fun PreviewSixWeeks() {
    PreviewMonthPage(mockPreviewMonth(previewSixWeeksMonth))
}

@Preview(name = "Wider than tall", widthDp = 800, heightDp = 400)
@Composable
private fun PreviewLandscape() {
    PreviewMonthPage(mockPreviewMonth(previewFiveWeeksMonth))
}

@Composable
private fun PreviewMonthPage(month: MonthUi, contentPadding: PaddingValues = PaddingValues()) {
    MaterialTheme {
        Surface {
            MonthPage(
                month = month,
                selectedDate = { month.month.onDay(20) },
                today = month.month.onDay(12),
                onDayClick = {},
                onEventClick = {},
                contentPadding = contentPadding,
            )
        }
    }
}
