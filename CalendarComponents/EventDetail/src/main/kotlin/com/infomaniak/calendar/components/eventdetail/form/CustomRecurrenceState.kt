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
package com.infomaniak.calendar.components.eventdetail.form

import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi.Rule.End
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi.Rule.Frequency
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi.Rule.MonthDay
import com.infomaniak.calendar.components.resources.R
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private const val DEFAULT_COUNT = 10

/** @param start The event's start date, which the options derive from. */
@Stable
class CustomRecurrenceState(
    val start: LocalDate,
    val intervalTextFieldState: TextFieldState,
    val frequency: MutableState<Frequency>,
    val daysOfWeek: MutableState<Set<DayOfWeek>>,
    val monthDay: MutableState<MonthDay>,
    val endType: MutableState<RecurrenceEndType>,
    val countTextFieldState: TextFieldState,
    val untilDate: MutableState<LocalDate>,
) {
    val interval: Int? get() = intervalTextFieldState.text.toString().toIntOrNull()?.takeIf { it > 0 }
    val count: Int? get() = countTextFieldState.text.toString().toIntOrNull()?.takeIf { it > 0 }

    fun toRecurrence(): RecurrenceUi.Rule? {
        val interval = interval ?: return null
        val isWeekly = frequency.value == Frequency.Weekly
        if (isWeekly && daysOfWeek.value.isEmpty()) return null

        return RecurrenceUi.Rule(
            frequency = frequency.value,
            interval = interval,
            // Only the start's day repeats as no day at all, which is what the weekly preset holds
            daysOfWeek = if (isWeekly) daysOfWeek.value.takeUnless { it == setOf(start.dayOfWeek) }.orEmpty() else emptySet(),
            monthDay = if (frequency.value == Frequency.Monthly || frequency.value == Frequency.Yearly) {
                monthDay.value
            } else {
                MonthDay.StartDay
            },
            end = when (endType.value) {
                RecurrenceEndType.Never -> End.Never
                RecurrenceEndType.AfterCount -> End.AfterCount(count ?: return null)
                RecurrenceEndType.Until -> End.Until(untilDate.value)
            },
        )
    }
}

enum class RecurrenceEndType(@StringRes val labelRes: Int) {
    Never(R.string.recurrenceEndNever),
    AfterCount(R.string.recurrenceEndAfterCount),
    Until(R.string.recurrenceEndUntilDate),
}

/** Starts from [initialRecurrence] when it's a rule, otherwise from every week on the [start]'s day. */
@Composable
fun rememberCustomRecurrenceState(start: LocalDate, initialRecurrence: RecurrenceUi): CustomRecurrenceState {
    val rule = initialRecurrence as? RecurrenceUi.Rule
    val end = rule?.end

    return CustomRecurrenceState(
        start = start,
        intervalTextFieldState = rememberTextFieldState((rule?.interval ?: 1).toString()),
        frequency = rememberSaveable { mutableStateOf(rule?.frequency ?: Frequency.Weekly) },
        daysOfWeek = rememberSaveable { mutableStateOf(rule?.daysOfWeek?.ifEmpty { null } ?: setOf(start.dayOfWeek)) },
        monthDay = rememberSaveable { mutableStateOf(rule?.monthDay ?: MonthDay.StartDay) },
        endType = rememberSaveable {
            mutableStateOf(
                when (end) {
                    null, End.Never -> RecurrenceEndType.Never
                    is End.AfterCount -> RecurrenceEndType.AfterCount
                    is End.Until -> RecurrenceEndType.Until
                },
            )
        },
        countTextFieldState = rememberTextFieldState(((end as? End.AfterCount)?.count ?: DEFAULT_COUNT).toString()),
        untilDate = rememberSaveable { mutableStateOf((end as? End.Until)?.date ?: start.plus(1, DateTimeUnit.MONTH)) },
    )
}
