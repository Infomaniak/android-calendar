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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.component.DateState
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

private const val RANGE_MONTHS = 100

@Composable
internal fun CollapsedCalendar(selectedDate: LocalDate, onDayClick: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    val startDate = remember { selectedDate.minus(RANGE_MONTHS, DateTimeUnit.MONTH) }
    val endDate = remember { selectedDate.plus(RANGE_MONTHS, DateTimeUnit.MONTH) }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    val weekState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = selectedDate,
        firstDayOfWeek = firstDayOfWeek,
    )

    WeekCalendar(
        state = weekState,
        weekHeader = { DaysOfWeekTitle(firstDayOfWeek) },
        dayContent = { day ->
            Day(
                dateState = when {
                    day.date == selectedDate -> DateState.Selected
                    day.date == today -> DateState.Today
                    day.position == WeekDayPosition.RangeDate -> DateState.None
                    else -> DateState.NotMonth
                },
                onClick = { onDayClick(day.date) },
                date = day.date,
            )
        },
        modifier = modifier,
    )
}

@Composable
@Preview
private fun CollapsedCalendarPreview() {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Surface {
        CollapsedCalendar(selectedDate = today, onDayClick = {})
    }
}
