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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinDayOfWeek
import kotlin.time.Clock

private const val RANGE_MONTHS = 100

@Composable
fun ExpandableCalendar(
    isExpanded: () -> Boolean,
    selectedDate: () -> LocalDate,
    onDayClick: (LocalDate) -> Unit,
    weekNumbering: WeekNumbering,
    modifier: Modifier = Modifier,
) {
    val firstDayOfWeek = remember(weekNumbering) { weekNumbering.firstDayOfWeek.toKotlinDayOfWeek() }
    val headerState = rememberCalendarHeaderState()
    var headerWidth by remember { mutableIntStateOf(0) }

    Box(modifier = modifier) {
        SharedTransitionLayout {
            val sharedTransitionScope = this
            AnimatedContent(
                targetState = isExpanded(),
                label = "calendarExpansion",
            ) { expanded ->
                if (expanded) {
                    ExpandedCalendar(
                        selectedDate = selectedDate,
                        onDayClick = onDayClick,
                        weekNumbering = weekNumbering,
                        monthRange = RANGE_MONTHS,
                        headerState = headerState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = this,
                    )
                } else {
                    CollapsedCalendar(
                        selectedDate = selectedDate,
                        onDayClick = onDayClick,
                        weekNumbering = weekNumbering,
                        monthRange = RANGE_MONTHS,
                        headerState = headerState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }

        DayOfWeekOverlayHeader(
            headerWidth = { headerWidth },
            updateHeaderWidth = { headerWidth = it },
            firstDayOfWeek = firstDayOfWeek,
            headerState = headerState,
            isExpanded = isExpanded,
        )
    }
}

@Composable
private fun DayOfWeekOverlayHeader(
    headerWidth: () -> Int,
    updateHeaderWidth: (Int) -> Unit,
    firstDayOfWeek: DayOfWeek,
    headerState: CalendarHeaderState,
    isExpanded: () -> Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .onSizeChanged { updateHeaderWidth(it.width) },
    ) {
        DaysOfWeekTitle(
            firstDayOfWeek = firstDayOfWeek,
            modifier = Modifier.graphicsLayer { translationX = headerState.offset(isExpanded()) },
        )
        DaysOfWeekTitle(
            firstDayOfWeek = firstDayOfWeek,
            modifier = Modifier.graphicsLayer { translationX = headerState.offset(isExpanded()) + headerWidth() },
        )
    }
}

@Composable
@Preview
private fun ExpandableCalendarCollapsedPreview() {
    Surface {
        ExpandableCalendar(
            selectedDate = { Clock.today() },
            onDayClick = {},
            isExpanded = { false },
            weekNumbering = WeekNumbering.ISO_8601,
        )
    }
}

@Composable
@Preview
private fun ExpandableCalendarExpandedPreview() {
    Surface {
        ExpandableCalendar(
            selectedDate = { Clock.today() },
            onDayClick = {},
            isExpanded = { true },
            weekNumbering = WeekNumbering.ISO_8601,
        )
    }
}
