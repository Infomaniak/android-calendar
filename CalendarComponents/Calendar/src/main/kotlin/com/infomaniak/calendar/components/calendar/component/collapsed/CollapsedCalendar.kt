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
import com.infomaniak.calendar.components.calendar.modifier.FollowExternalSelection
import com.infomaniak.calendar.components.calendar.modifier.SyncHeaderOffset
import com.infomaniak.calendar.components.calendar.modifier.pagedSwipe
import com.infomaniak.calendar.components.calendar.component.collapsed.rememberWeekPager
import com.infomaniak.calendar.components.foundation.component.DateState
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.state.rememberToday
import com.infomaniak.calendar.components.foundation.utils.startOfWeek
import com.infomaniak.core.common.utils.today
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.WeekDay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinDayOfWeek
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

/** Margins held on each side at startup, so the first swipes never have to grow the range. */
private const val INITIAL_MARGINS = 2

@Composable
internal fun CollapsedCalendar(
    monthMargin: Int,
    selectedDate: () -> LocalDate,
    weekNumbering: WeekNumbering,
    onDayClick: (LocalDate) -> Unit,
    eventsDots: () -> Map<LocalDate, List<EventColorsUi>>,
    modifier: Modifier = Modifier,
    headerState: CalendarHeaderState = rememberCalendarHeaderState(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val firstDayOfWeek = remember { weekNumbering.firstDayOfWeek.toKotlinDayOfWeek() }
    val today by rememberToday()

    val initialWeekStart = remember { selectedDate().startOfWeek(firstDayOfWeek) }

    val weekState = rememberWeekCalendarState(
        startDate = remember { initialWeekStart.minus(INITIAL_MARGINS * monthMargin, DateTimeUnit.MONTH) },
        endDate = remember { initialWeekStart.plus(INITIAL_MARGINS * monthMargin, DateTimeUnit.MONTH) },
        firstVisibleWeekDate = initialWeekStart,
        firstDayOfWeek = firstDayOfWeek,
    )
    val pager = rememberWeekPager(state = weekState, firstDayOfWeek = firstDayOfWeek, monthMargin = monthMargin)

    val sharedElementDates by remember {
        derivedStateOf { weekState.firstVisibleWeek.days.mapTo(mutableSetOf()) { it.date } }
    }

    FollowExternalSelection(pager = pager, selectedDate = selectedDate)

    SyncHeaderOffset(
        scrollableState = weekState,
        headerState = headerState,
        layoutInfo = { weekState.layoutInfo },
        setOffsetSource = { headerState.setCollapsedOffsetSource(it) },
    )

    WeekCalendar(
        state = weekState,
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
                isSharedElementEnabled = day.date in sharedElementDates,
                dotsFor = { eventsDots()[day.date].orEmpty() },
            )
        },
        modifier = modifier.pagedSwipe(pager = pager, onPageSwiped = onDayClick),
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
    dotsFor: () -> List<EventColorsUi>,
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
        dotsFor = dotsFor,
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
            monthMargin = 12,
            selectedDate = { Clock.today() },
            onDayClick = {},
            weekNumbering = WeekNumbering.ISO_8601,
            eventsDots = { emptyMap() },
        )
    }
}
