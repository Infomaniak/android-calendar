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
import com.infomaniak.calendar.components.foundation.utils.startOfWeek
import com.infomaniak.core.common.utils.today
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.WeekDay
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinDayOfWeek
import kotlin.time.Clock

private const val DAYS_IN_WEEK = 7

/**
 * The collapsed (single week) form of [ExpandableCalendar]. Its expanded counterpart is
 * [ExpandedCalendar]; both are built the same way and any change here likely applies there too.
 *
 * @param monthRange how far back and forward the calendar can be scrolled, in months.
 * @param selectedDate the highlighted day, passed as a lambda so the state read can be deferred
 * down to the individual day cells (see [DayContent]). Reading it here would subscribe the whole
 * calendar to every selection change.
 * @param onDayClick called when the user taps a day.
 * @param onVisibleWeekChange called when a swipe lands on another week, with the first day of that
 * week. Fired as soon as the gesture ends, before the settle animation runs.
 * @param headerState shared with [ExpandableCalendar]'s overlay header, which needs this pager's
 * scroll offset to translate along with the day columns.
 */
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

    // Snapshot of the selection taken when this calendar enters composition. Using a keyless
    // `remember` means the lambda runs once, so this scope stops being subscribed to the
    // selection state after the first change instead of recomposing on every one.
    val initialWeekStart = remember { selectedDate().startOfWeek(firstDayOfWeek) }

    // `rememberWeekCalendarState` passes these as `rememberSaveable` inputs, so any change to them
    // recreates the state from scratch, teleporting the calendar to `firstVisibleWeekDate` with no
    // animation. They must therefore stay frozen; scrolling is done through the state instead.
    val weekState = rememberWeekCalendarState(
        startDate = remember { initialWeekStart.minus(monthRange, DateTimeUnit.MONTH) },
        endDate = remember { initialWeekStart.plus(monthRange, DateTimeUnit.MONTH) },
        firstVisibleWeekDate = initialWeekStart,
        firstDayOfWeek = firstDayOfWeek,
    )

    // Days of the week currently on screen. Only those take part in the expand/collapse shared
    // transition: the off-screen weeks hold the same dates as the expanded month grid, and letting
    // them match would make the transition pick an arbitrary duplicate.
    val visibleWeekDates by remember {
        derivedStateOf { weekState.firstVisibleWeek.days.mapTo(mutableSetOf()) { it.date } }
    }

    FollowExternalSelection(
        state = weekState,
        selectedPage = { selectedDate().startOfWeek(firstDayOfWeek) },
        currentPage = { weekState.firstVisibleWeek.days.first().date },
        animateScrollToPage = { weekState.animateScrollToWeek(it) },
    )

    SyncHeaderOffset(
        state = weekState,
        headerState = headerState,
        layoutInfo = { weekState.layoutInfo },
        setSource = headerState::setCollapsedOffsetSource,
    )

    WeekCalendar(
        state = weekState,
        // Required for the layout, not for the fling: this is what gives each week the viewport
        // width and each day its `weight(1f)`. With `false` the days keep their intrinsic width and
        // `Day`'s `fillMaxWidth()` makes each one span the whole screen.
        calendarScrollPaged = true,
        // The list must not consume gestures: `pagedSwipe` below drives the scrolling so it can
        // decide the destination week itself, at drag end, instead of reading it back afterwards.
        userScrollEnabled = false,
        weekHeader = {
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
                isSharedElementEnabled = day.date in visibleWeekDates,
            )
        },
        modifier = modifier.pagedSwipe(
            state = weekState,
            firstVisibleItemOffset = { weekState.layoutInfo.firstVisibleItemOffset() },
            currentPage = { weekState.firstVisibleWeek.days.first().date },
            // A page is a week, so stepping means moving a whole week. The returned value is kept by
            // `pagedSwipe` as the starting point of a chained swipe, so quick successive swipes
            // report every week instead of repeating the same one.
            onPageChange = { from, step ->
                from.plus(step * DAYS_IN_WEEK, DateTimeUnit.DAY).also(onVisibleWeekChange)
            },
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
    // Both lambdas are read here rather than by the caller, so a selection change invalidates only
    // the cells whose state actually changes. `derivedStateOf` filters on the result: every visible
    // cell re-runs this cheap `when`, but only the day losing `Selected` and the one gaining it
    // recompose. The `day` key is required because lazy layouts reuse slots across items.
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
