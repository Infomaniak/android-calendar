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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinDayOfWeek
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

/**
 * The expanded (full month) form of [ExpandableCalendar]. Its collapsed counterpart is
 * [CollapsedCalendar]; both are built the same way and any change here likely applies there too.
 *
 * @param monthRange how far back and forward the calendar can be scrolled, in months.
 * @param selectedDate the highlighted day, passed as a lambda so the state read can be deferred
 * down to the individual day cells (see [DayContent]). Reading it here would subscribe the whole
 * calendar to every selection change.
 * @param onDayClick called when the user taps a day.
 * @param onVisibleMonthChange called when a swipe lands on another month. Fired as soon as the
 * gesture ends, before the settle animation runs.
 * @param headerState shared with [ExpandableCalendar]'s overlay header, which needs this pager's
 * scroll offset to translate along with the day columns.
 */
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

    // Snapshot of the selection taken when this calendar enters composition. Using a keyless
    // `remember` means the lambda runs once, so this scope stops being subscribed to the
    // selection state after the first change instead of recomposing on every one.
    val initialMonth = remember { selectedDate().yearMonth }

    // `rememberCalendarState` passes these as `rememberSaveable` inputs, so any change to them
    // recreates the state from scratch, teleporting the calendar to `firstVisibleMonth` with no
    // animation. They must therefore stay frozen; scrolling is done through the state instead.
    val monthState = rememberCalendarState(
        startMonth = remember { initialMonth.minus(monthRange, DateTimeUnit.MONTH) },
        endMonth = remember { initialMonth.plus(monthRange, DateTimeUnit.MONTH) },
        firstVisibleMonth = initialMonth,
        firstDayOfWeek = firstDayOfWeek,
    )

    val today by rememberToday()

    // Days of the month currently on screen. Only those take part in the expand/collapse shared
    // transition: neighbouring months hold the same dates as in/out dates, and letting them match
    // would make the transition pick an arbitrary duplicate.
    val visibleMonthDays by remember { derivedStateOf { monthState.firstVisibleMonth.weekDays.flatten().toSet() } }

    FollowExternalSelection(
        state = monthState,
        selectedPage = { selectedDate().yearMonth },
        currentPage = { monthState.firstVisibleMonth.yearMonth },
        animateScrollToPage = { monthState.animateScrollToMonth(it) },
    )

    SyncHeaderOffset(
        state = monthState,
        headerState = headerState,
        layoutInfo = { monthState.layoutInfo },
        setSource = headerState::setExpandedOffsetSource,
    )

    HorizontalCalendar(
        state = monthState,
        // Only affects the fling here, which never runs: month items always fill the viewport
        // width regardless of this flag (unlike weeks, where `CollapsedCalendar` needs `true`).
        calendarScrollPaged = false,
        // The list must not consume gestures: `pagedSwipe` below drives the scrolling so it can
        // decide the destination month itself, at drag end, instead of reading it back afterwards.
        userScrollEnabled = false,
        monthHeader = {
            DaysOfWeekTitle(
                firstDayOfWeek = firstDayOfWeek,
                modifier = Modifier
                    .alpha(0f) // Reserves the space to keep the swiping on header
                    .clearAndSetSemantics {}, // The visible header is the overlay one, announce it only once
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
                firstVisibleItemOffset = { monthState.layoutInfo.firstVisibleItemOffset() },
                currentPage = { monthState.firstVisibleMonth.yearMonth },
                // A page is a month, so stepping means moving a whole month. The returned value is
                // kept by `pagedSwipe` as the starting point of a chained swipe, so quick successive
                // swipes report every month instead of repeating the same one.
                onPageChange = { from, step ->
                    from.plus(step, DateTimeUnit.MONTH).also(onVisibleMonthChange)
                },
            )
            // Months have 4 to 6 week rows, so the calendar height changes from page to page.
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
    // Both lambdas are read here rather than by the caller, so a selection change invalidates only
    // the cells whose state actually changes. `derivedStateOf` filters on the result: every visible
    // cell re-runs this cheap `when`, but only the day losing `Selected` and the one gaining it
    // recompose. The `day` key is required because lazy layouts reuse slots across items.
    val dateState by remember(day) {
        derivedStateOf {
            // In/out dates repeat a neighbouring month's days, so the same date can be on screen
            // twice while two pages overlap. Only the month that owns the date may highlight it,
            // otherwise the selection appears on two cells at once during a swipe.
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
