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

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinDayOfWeek
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

private const val RANGE_MONTHS = 100

@Composable
internal fun ExpandedCalendar(
    selectedDate: () -> LocalDate,
    weekNumbering: WeekNumbering,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstDayOfWeek = remember { weekNumbering.firstDayOfWeek.toKotlinDayOfWeek() }

    val today by rememberToday()

    val initialMonth by remember { derivedStateOf { selectedDate().yearMonth } }
    val startMonth by remember { derivedStateOf { selectedDate().yearMonth.minus(RANGE_MONTHS, DateTimeUnit.MONTH) } }
    val endMonth by remember { derivedStateOf { selectedDate().yearMonth.plus(RANGE_MONTHS, DateTimeUnit.MONTH) } }

    val monthState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = initialMonth,
        firstDayOfWeek = firstDayOfWeek,
    )

    HorizontalCalendar(
        state = monthState,
        monthHeader = { DaysOfWeekTitle(firstDayOfWeek) },
        dayContent = { day ->
            DayContent(day = day, selectedDate = selectedDate, today = { today }, onDayClick = onDayClick)
        },
        modifier = modifier,
    )
}

@Composable
private fun DayContent(
    day: CalendarDay,
    selectedDate: () -> LocalDate,
    today: () -> LocalDate,
    onDayClick: (LocalDate) -> Unit,
) {
    val dateState by remember {
        derivedStateOf {
            when {
                day.date == selectedDate() -> DateState.Selected
                day.date == today() -> DateState.Today
                day.position == DayPosition.MonthDate -> DateState.None
                else -> DateState.NotMonth
            }
        }
    }

    Day(
        dateState = dateState,
        onClick = { onDayClick(day.date) },
        date = day.date,
    )
}

@Composable
@Preview
private fun ExpandedCalendarPreview() {
    Surface {
        ExpandedCalendar(selectedDate = { Clock.today() }, onDayClick = {}, weekNumbering = WeekNumbering.ISO_8601)
    }
}
