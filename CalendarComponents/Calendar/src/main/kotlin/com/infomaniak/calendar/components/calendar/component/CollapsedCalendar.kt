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

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.component.DateState
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.state.rememberToday
import com.infomaniak.calendar.components.foundation.utils.startOfWeek
import com.infomaniak.core.common.utils.today
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.WeekDay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinDayOfWeek
import kotlin.time.Clock

private const val DAYS_IN_WEEK = 7

@Composable
internal fun CollapsedCalendar(
    monthRange: Int,
    selectedDate: () -> LocalDate,
    weekNumbering: WeekNumbering,
    onDayClick: (LocalDate) -> Unit,
    onVisibleWeekChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    headerState: CalendarHeaderState = rememberCalendarHeaderState(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val firstDayOfWeek = remember { weekNumbering.firstDayOfWeek.toKotlinDayOfWeek() }
    val today by rememberToday()

    val initialWeekStart = remember { selectedDate().startOfWeek(firstDayOfWeek) }

    val weekState = rememberWeekCalendarState(
        startDate = remember { initialWeekStart.minus(monthRange, DateTimeUnit.MONTH) },
        endDate = remember { initialWeekStart.plus(monthRange, DateTimeUnit.MONTH) },
        firstVisibleWeekDate = initialWeekStart,
        firstDayOfWeek = firstDayOfWeek,
    )

    val visibleWeekDates by remember {
        derivedStateOf { weekState.firstVisibleWeek.days.mapTo(mutableSetOf()) { it.date } }
    }

    // Follow external selection changes (day click, jump to today, deep link).
    LaunchedEffect(weekState) {
        snapshotFlow { selectedDate().startOfWeek(firstDayOfWeek) }.collectLatest { weekStart ->
            // Wait for any ongoing gesture or settle instead of dropping the request.
            snapshotFlow { weekState.isScrollInProgress }.first { !it }
            if (weekStart != weekState.firstVisibleWeek.days.first().date) {
                weekState.animateScrollToWeek(weekStart)
            }
        }
    }

    LaunchedEffect(weekState, headerState) {
        headerState.setCollapsedOffsetSource {
            val layoutInfo = weekState.layoutInfo
            val pageWidth = layoutInfo.viewportSize.width
            if (pageWidth <= 0) return@setCollapsedOffsetSource 0f
            val info = layoutInfo.visibleItemsInfo.firstOrNull() ?: return@setCollapsedOffsetSource 0f
            // Absolute scroll position in px: stays continuous when the leading item changes.
            val scrolled = info.index.toLong() * pageWidth - info.offset
            -(scrolled.toFloat().mod(pageWidth.toFloat()))
        }
    }

    WeekCalendar(
        state = weekState,
        // Keeps one-week-per-viewport layout. The paged fling never runs since
        // userScrollEnabled is false: our pagedSwipe drives the scrolling.
        calendarScrollPaged = true,
        userScrollEnabled = false,
        weekHeader = {
            DaysOfWeekTitle(
                firstDayOfWeek = firstDayOfWeek,
                modifier = Modifier
                    .alpha(0f) // Reserves the space to keep the swiping on header
                    .clearAndSetSemantics {},
            )
        },
        dayContent = { day ->
            DayContent(
                day = day,
                selectedDate = selectedDate,
                today = { today },
                onDayClick = onDayClick,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                isSharedElementEnabled = day.date in visibleWeekDates,
            )
        },
        modifier = modifier.pagedSwipe(
            state = weekState,
            firstVisibleItemOffset = { weekState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset ?: 0 },
            currentPage = { weekState.firstVisibleWeek.days.first().date },
            onPageChange = { from, step -> onVisibleWeekChange(from.plus(step * DAYS_IN_WEEK, DateTimeUnit.DAY)) },
        ),
    )
}

@Composable
private fun DayContent(
    day: WeekDay,
    selectedDate: () -> LocalDate,
    today: () -> LocalDate,
    onDayClick: (LocalDate) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    isSharedElementEnabled: Boolean,
) {
    val dateState by remember(day) {
        derivedStateOf {
            when (day.date) {
                selectedDate() -> DateState.Selected
                today() -> DateState.Today
                else -> DateState.None
            }
        }
    }

    Day(
        dateState = dateState,
        onClick = { onDayClick(day.date) },
        date = day.date,
        modifier = Modifier.daySharedElement(
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            date = day.date,
            enabled = isSharedElementEnabled,
        ),
    )
}

@Composable
@Preview
private fun CollapsedCalendarPreview() {
    Surface {
        CollapsedCalendar(
            monthRange = 3,
            selectedDate = { Clock.today() },
            onDayClick = {},
            onVisibleWeekChange = {},
            weekNumbering = WeekNumbering.ISO_8601,
        )
    }
}
