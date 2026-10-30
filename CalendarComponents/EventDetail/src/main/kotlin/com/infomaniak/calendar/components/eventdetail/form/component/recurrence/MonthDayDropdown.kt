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
package com.infomaniak.calendar.components.eventdetail.form.component.recurrence

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.form.component.OptionsDropdown
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi.Rule.MonthDay
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDayAndMonth
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDayName
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatMonthName
import com.infomaniak.calendar.components.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearMonth

private const val DAYS_IN_WEEK = 7
private const val LAST_POSITION = -1

/** Where in the month the event repeats, on the [start]'s day or on its weekday; [isYearly] adds the month. */
@Composable
internal fun MonthDayDropdown(
    start: LocalDate,
    isYearly: Boolean,
    selectedMonthDay: MonthDay,
    onMonthDaySelect: (MonthDay) -> Unit,
    modifier: Modifier = Modifier,
) {
    OptionsDropdown(
        options = monthDayOptions(start),
        selectedOption = selectedMonthDay,
        onOptionSelect = onMonthDaySelect,
        optionText = { monthDay -> monthDayLabel(monthDay, start, isYearly) },
        modifier = modifier,
    )
}

private fun monthDayOptions(start: LocalDate): List<MonthDay> = buildList {
    add(MonthDay.StartDay)

    val position = (start.day - 1) / DAYS_IN_WEEK + 1
    if (position < 5) add(MonthDay.Weekday(position, start.dayOfWeek))

    val isInLastWeek = start.day + DAYS_IN_WEEK > start.yearMonth.numberOfDays
    if (isInLastWeek) add(MonthDay.Weekday(LAST_POSITION, start.dayOfWeek))
}

@Composable
private fun monthDayLabel(monthDay: MonthDay, start: LocalDate, isYearly: Boolean): String = when (monthDay) {
    MonthDay.StartDay -> if (isYearly) {
        stringResource(R.string.recurrenceOnDate, start.formatDayAndMonth())
    } else {
        stringResource(R.string.recurrenceOnMonthDay, start.day)
    }
    is MonthDay.Weekday -> {
        val position = stringResource(
            when (monthDay.position) {
                1 -> R.string.recurrenceOrdinalFirst
                2 -> R.string.recurrenceOrdinalSecond
                3 -> R.string.recurrenceOrdinalThird
                4 -> R.string.recurrenceOrdinalFourth
                else -> R.string.recurrenceOrdinalLast
            },
        )
        val dayName = monthDay.dayOfWeek.formatDayName()

        if (isYearly) {
            stringResource(R.string.recurrenceInMonthOnWeekday, start.yearMonth.formatMonthName(), position, dayName)
        } else {
            stringResource(R.string.recurrenceOnWeekday, position, dayName)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            MonthDayDropdown(
                start = LocalDate(2026, 12, 21),
                isYearly = false,
                selectedMonthDay = MonthDay.StartDay,
                onMonthDaySelect = {},
            )
        }
    }
}
