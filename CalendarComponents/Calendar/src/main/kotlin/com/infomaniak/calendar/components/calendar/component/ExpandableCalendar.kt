/*
 * Infomaniak Calendar - Android
 * Copyright (C) 2026-2026 Infomaniak Network SA
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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.component.DateState
import com.infomaniak.calendar.components.foundation.component.DayCircle
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.DayFormatter.toSimpleDayName
import com.infomaniak.core.ui.compose.margin.Margin
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

@Composable
fun ExpandableCalendar(
    isExpanded: () -> Boolean,
    selectedDate: () -> LocalDate,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    val startMonth = remember { selectedDate().yearMonth.minus(RANGE_MONTHS, DateTimeUnit.MONTH) }
    val endMonth = remember { selectedDate().yearMonth.plus(RANGE_MONTHS, DateTimeUnit.MONTH) }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val initialDate = remember { selectedDate() }

    val monthState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = initialDate.yearMonth,
        firstDayOfWeek = firstDayOfWeek,
    )
    val weekState = rememberWeekCalendarState(
        startDate = startMonth.firstDay,
        endDate = endMonth.lastDay,
        firstVisibleWeekDate = initialDate,
        firstDayOfWeek = firstDayOfWeek,
    )

    SyncCalendarsWithSelectedDate(isExpanded, selectedDate, firstDayOfWeek, monthState, weekState)

    Column(modifier) {
        AnimatedContent(targetState = isExpanded(), label = "calendar_expansion") { expanded ->
            if (expanded) {
                HorizontalCalendar(
                    state = monthState,
                    monthHeader = { DaysOfWeekTitle(firstDayOfWeek) },
                    dayContent = { day ->
                        Day(
                            day = day,
                            isSelected = day.date == selectedDate(),
                            isToday = day.date == today,
                            onClick = onDayClick,
                        )
                    },
                    modifier = Modifier.animateContentSize(),
                )
            } else {
                WeekCalendar(
                    state = weekState,
                    weekHeader = { DaysOfWeekTitle(firstDayOfWeek) },
                    dayContent = { day ->
                        Day(
                            day = CalendarDay(day.date, DayPosition.MonthDate),
                            isSelected = day.date == selectedDate(),
                            isToday = day.date == today,
                            onClick = onDayClick,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SyncCalendarsWithSelectedDate(
    isExpanded: () -> Boolean,
    selectedDate: () -> LocalDate,
    firstDayOfWeek: DayOfWeek,
    monthState: CalendarState,
    weekState: WeekCalendarState,
) {
    LaunchedEffect(Unit) {
        snapshotFlow {
            val date = selectedDate()
            if (isExpanded()) {
                CalendarPageTarget.Month(date.yearMonth)
            } else {
                CalendarPageTarget.Week(firstDay = date.firstDayOfWeek(firstDayOfWeek))
            }
        }
            .distinctUntilChanged()
            .collect { target ->
                when (target) {
                    is CalendarPageTarget.Month -> monthState.animateScrollToMonth(month = target.month)
                    is CalendarPageTarget.Week -> weekState.animateScrollToWeek(date = target.firstDay)
                }
            }
    }
}

private sealed interface CalendarPageTarget {
    data class Month(val month: YearMonth) : CalendarPageTarget
    data class Week(val firstDay: LocalDate) : CalendarPageTarget
}

private fun LocalDate.firstDayOfWeek(firstDayOfWeek: DayOfWeek): LocalDate {
    val daysSinceWeekStart = (dayOfWeek.isoDayNumber - firstDayOfWeek.isoDayNumber + 7) % 7
    return minus(daysSinceWeekStart, DateTimeUnit.DAY)
}

@Composable
private fun DaysOfWeekTitle(firstDayOfWeek: DayOfWeek) {
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek(firstDayOfWeek).forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.name.first().toString(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun Day(day: CalendarDay, isSelected: Boolean, isToday: Boolean, onClick: (LocalDate) -> Unit) {
    val isCurrentMonth = day.position == DayPosition.MonthDate
    val locale = LocalLocale.current.platformLocale

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Margin.Micro),
        contentAlignment = Alignment.Center,
    ) {
        DayCircle(
            state = when {
                isSelected -> DateState.Selected
                isToday -> DateState.Today
                else -> DateState.None
            },
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onClick(day.date) },
        ) {
            Text(
                text = day.date.toSimpleDayName(locale),
                color = if (isCurrentMonth) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.38f),
            )
        }
    }
}

private const val RANGE_MONTHS = 100
