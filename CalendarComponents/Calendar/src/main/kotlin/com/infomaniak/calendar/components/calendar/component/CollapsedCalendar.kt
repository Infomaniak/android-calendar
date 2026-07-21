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
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.WeekDayPosition
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinDayOfWeek
import kotlin.time.Clock

@Composable
internal fun CollapsedCalendar(
    monthRange: Int,
    selectedDate: () -> LocalDate,
    weekNumbering: WeekNumbering,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstDayOfWeek = remember { weekNumbering.firstDayOfWeek.toKotlinDayOfWeek() }

    val initialDate = remember { selectedDate() }
    val todayState = rememberToday()

    val weekState = rememberWeekCalendarState(
        startDate = remember { initialDate.minus(monthRange, DateTimeUnit.MONTH) },
        endDate = remember { initialDate.plus(monthRange, DateTimeUnit.MONTH) },
        firstVisibleWeekDate = initialDate,
        firstDayOfWeek = firstDayOfWeek,
    )

    WeekCalendar(
        state = weekState,
        weekHeader = { DaysOfWeekTitle(firstDayOfWeek) },
        dayContent = { day ->
            val dateState by remember(day) {
                derivedStateOf {
                    when {
                        day.date == selectedDate() -> DateState.Selected
                        day.date == todayState.value -> DateState.Today
                        day.position == WeekDayPosition.RangeDate -> DateState.None
                        else -> DateState.NotMonth
                    }
                }
            }
            Day(
                dateState = dateState,
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
    Surface {
        CollapsedCalendar(
            monthRange = 3,
            selectedDate = { Clock.today() },
            onDayClick = {},
            weekNumbering = WeekNumbering.ISO_8601,
        )
    }
}
