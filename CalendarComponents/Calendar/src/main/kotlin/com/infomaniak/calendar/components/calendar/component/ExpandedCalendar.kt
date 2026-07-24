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
import androidx.compose.animation.animateContentSize
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
import com.infomaniak.core.common.utils.today
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinDayOfWeek
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

@Composable
internal fun ExpandedCalendar(
    monthRange: Int,
    selectedDate: () -> LocalDate,
    weekNumbering: WeekNumbering,
    onDayClick: (LocalDate) -> Unit,
    onVisibleMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
    headerState: CalendarHeaderState = rememberCalendarHeaderState(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val firstDayOfWeek = remember { weekNumbering.firstDayOfWeek.toKotlinDayOfWeek() }

    val initialMonth = remember { selectedDate().yearMonth }
    val monthState = rememberCalendarState(
        startMonth = remember { initialMonth.minus(monthRange, DateTimeUnit.MONTH) },
        endMonth = remember { initialMonth.plus(monthRange, DateTimeUnit.MONTH) },
        firstVisibleMonth = initialMonth,
        firstDayOfWeek = firstDayOfWeek,
    )

    val today by rememberToday()
    val visibleMonthDays by remember { derivedStateOf { monthState.firstVisibleMonth.weekDays.flatten().toSet() } }

    // Follow external selection changes (day click, jump to today, deep link).
    LaunchedEffect(monthState) {
        snapshotFlow { selectedDate().yearMonth }.collectLatest { month ->
            // Wait for any ongoing gesture or settle instead of dropping the request.
            snapshotFlow { monthState.isScrollInProgress }.first { !it }
            if (month != monthState.firstVisibleMonth.yearMonth) monthState.animateScrollToMonth(month)
        }
    }

    LaunchedEffect(monthState, headerState) {
        headerState.setExpandedOffsetSource {
            val layoutInfo = monthState.layoutInfo
            val pageWidth = layoutInfo.viewportSize.width
            if (pageWidth <= 0) return@setExpandedOffsetSource 0f
            val info = layoutInfo.visibleItemsInfo.firstOrNull() ?: return@setExpandedOffsetSource 0f
            // Absolute scroll position in px: stays continuous when the leading item changes.
            val scrolled = info.index.toLong() * pageWidth - info.offset
            -(scrolled.toFloat().mod(pageWidth.toFloat()))
        }
    }

    HorizontalCalendar(
        state = monthState,
        calendarScrollPaged = false,
        userScrollEnabled = false,
        monthHeader = {
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
                isSharedElementEnabled = day in visibleMonthDays,
            )
        },
        modifier = modifier
            .pagedSwipe(
                state = monthState,
                firstVisibleItemOffset = { monthState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset ?: 0 },
                currentPage = { monthState.firstVisibleMonth.yearMonth },
                onPageChange = { from, step ->
                    from.plus(step, DateTimeUnit.MONTH).also(onVisibleMonthChange)
                },
            )
            .animateContentSize(),
    )
}

@Composable
private fun DayContent(
    day: CalendarDay,
    selectedDate: () -> LocalDate,
    today: () -> LocalDate,
    onDayClick: (LocalDate) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    isSharedElementEnabled: Boolean,
) {
    val dateState by remember(day) {
        derivedStateOf {
            val isInMonth = day.position == DayPosition.MonthDate
            when {
                isInMonth && day.date == selectedDate() -> DateState.Selected
                isInMonth && day.date == today() -> DateState.Today
                isInMonth -> DateState.None
                else -> DateState.NotMonth
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
private fun ExpandedCalendarPreview() {
    Surface {
        ExpandedCalendar(
            monthRange = 3,
            selectedDate = { Clock.today() },
            onDayClick = {},
            onVisibleMonthChange = {},
            weekNumbering = WeekNumbering.ISO_8601,
        )
    }
}
