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
package com.infomaniak.calendar.components.calendar.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.lerp
import com.infomaniak.calendar.components.calendar.component.expanded.ExpandedCalendar
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.utils.startOfWeek
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toKotlinDayOfWeek
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

private const val MONTH_MARGIN = 1

private const val DAYS_IN_WEEK = 7

@Composable
fun ExpandableCalendar(
    expansionState: CalendarExpansionState,
    selectedDate: () -> LocalDate,
    onDayClick: (LocalDate) -> Unit,
    weekNumbering: WeekNumbering,
    eventsDots: () -> Map<LocalDate, List<EventColorsUi>>,
    modifier: Modifier = Modifier,
) {
    val firstDayOfWeek = remember(weekNumbering) { weekNumbering.firstDayOfWeek.toKotlinDayOfWeek() }

    val headerState = rememberCalendarHeaderState()

    var headerSize by remember { mutableStateOf(IntSize.Zero) }
    var collapsedHeight by remember { mutableIntStateOf(0) }

    val currentSelectedDate by rememberUpdatedState(selectedDate)
    val weeksAboveSelection by remember(firstDayOfWeek) {
        derivedStateOf { currentSelectedDate().weekRowsAboveCurrentRow(firstDayOfWeek) }
    }

    Layout(
        contents = listOf(
            {
                Column(
                    modifier = Modifier
                        // The header is drawn over the calendar and has no background of its own, so rows
                        // rolling past the top are clipped away instead of showing through the day names.
                        .drawWithContent {
                            clipRect(top = headerSize.height.toFloat()) { this@drawWithContent.drawContent() }
                        }
                        .graphicsLayer {
                            val weekRowHeight = (collapsedHeight - headerSize.height).toFloat()
                            translationY = -weeksAboveSelection * weekRowHeight * (EXPANDED - expansionState.progress)
                        },
                ) {
                    ExpandedCalendar(
                        selectedDate = selectedDate,
                        onDayClick = onDayClick,
                        weekNumbering = weekNumbering,
                        monthMargin = MONTH_MARGIN,
                        headerState = headerState,
                        eventsDots = eventsDots,
                        notMonthFraction = { expansionState.progress },
                    )
                    HorizontalMonthSelector(
                        selectedMonth = { selectedDate().yearMonth },
                        onMonthSelected = { month -> onDayClick(month.firstDay) },
                    )
                }
            },
            {
                CollapsedCalendar(
                    selectedDate = selectedDate,
                    onDayClick = onDayClick,
                    weekNumbering = weekNumbering,
                    monthMargin = MONTH_MARGIN,
                    headerState = headerState,
                    eventsDots = eventsDots,
                    modifier = Modifier.onSizeChanged { collapsedHeight = it.height },
                )
            },
            {
                DayOfWeekOverlayHeader(
                    headerWidth = { headerSize.width },
                    updateHeaderSize = { headerSize = it },
                    firstDayOfWeek = firstDayOfWeek,
                    headerState = headerState,
                    expansionProgress = { expansionState.progress },
                )
            },
        ),
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
    ) { (expandedMeasurables, collapsedMeasurables, headerMeasurables), constraints ->
        val childConstraints = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)

        val expanded = expandedMeasurables.first().measure(childConstraints)
        val collapsed = collapsedMeasurables.first().measure(childConstraints)
        val header = headerMeasurables.first().measure(childConstraints)

        expansionState.updateMetrics(dragRange = (expanded.height - collapsed.height).toFloat(), density = this)

        val progress = expansionState.progress

        layout(width = maxOf(expanded.width, collapsed.width), height = lerp(collapsed.height, expanded.height, progress)) {
            if (progress > COLLAPSED) expanded.placeRelative(0, 0) else collapsed.placeRelative(0, 0)
            header.placeRelative(0, 0)
        }
    }
}

@Composable
private fun DayOfWeekOverlayHeader(
    headerWidth: () -> Int,
    updateHeaderSize: (IntSize) -> Unit,
    firstDayOfWeek: DayOfWeek,
    headerState: CalendarHeaderState,
    expansionProgress: () -> Float,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .onSizeChanged(updateHeaderSize),
    ) {
        DaysOfWeekTitle(
            firstDayOfWeek = firstDayOfWeek,
            modifier = Modifier.graphicsLayer {
                translationX = headerState.offset(isExpanded = expansionProgress() > COLLAPSED)
            },
        )
        DaysOfWeekTitle(
            firstDayOfWeek = firstDayOfWeek,
            modifier = Modifier.graphicsLayer {
                translationX = headerState.offset(isExpanded = expansionProgress() > COLLAPSED) + headerWidth()
            },
        )
    }
}

private fun LocalDate.weekRowsAboveCurrentRow(firstDayOfWeek: DayOfWeek): Int {
    val firstRow = yearMonth.firstDay.startOfWeek(firstDayOfWeek)
    return firstRow.daysUntil(startOfWeek(firstDayOfWeek)) / DAYS_IN_WEEK
}

@Composable
@Preview
private fun ExpandableCalendarCollapsedPreview() {
    Surface {
        ExpandableCalendar(
            expansionState = rememberCalendarExpansionState(),
            selectedDate = { Clock.today() },
            onDayClick = {},
            weekNumbering = WeekNumbering.ISO_8601,
            eventsDots = { emptyMap() },
        )
    }
}

@Composable
@Preview
private fun ExpandableCalendarExpandedPreview() {
    Surface {
        ExpandableCalendar(
            expansionState = rememberCalendarExpansionState(initiallyExpanded = true),
            selectedDate = { Clock.today() },
            onDayClick = {},
            weekNumbering = WeekNumbering.ISO_8601,
            eventsDots = { emptyMap() },
        )
    }
}
