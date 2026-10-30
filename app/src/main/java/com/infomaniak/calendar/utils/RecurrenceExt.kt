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
package com.infomaniak.calendar.utils

import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi.Rule.End
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi.Rule.MonthDay
import com.infomaniak.multiplatform_calendar.core.domain.model.event.recurrenceRule.Frequency
import com.infomaniak.multiplatform_calendar.core.domain.model.event.recurrenceRule.RecurrenceRule
import com.infomaniak.multiplatform_calendar.core.domain.model.event.recurrenceRule.RecurrenceUntil
import com.infomaniak.multiplatform_calendar.core.domain.model.event.recurrenceRule.WeekDayNum
import com.infomaniak.multiplatform_calendar.core.domain.model.event.recurrenceRule.toLocalizedString
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import kotlinx.datetime.number

suspend fun RecurrenceUi.toLocalizedString(start: LocalDateTime): String? {
    val rule = this as? RecurrenceUi.Rule ?: return null
    return rule.toRecurrenceRule(start.date).toLocalizedString(start)
}

/** [start] gives the month a yearly rule repeats in, and KMP derives what the rule leaves out from it as well. */
fun RecurrenceUi.Rule.toRecurrenceRule(start: LocalDate): RecurrenceRule = RecurrenceRule(
    freq = when (frequency) {
        RecurrenceUi.Rule.Frequency.Daily -> Frequency.Daily
        RecurrenceUi.Rule.Frequency.Weekly -> Frequency.Weekly
        RecurrenceUi.Rule.Frequency.Monthly -> Frequency.Monthly
        RecurrenceUi.Rule.Frequency.Yearly -> Frequency.Yearly
    },
    interval = interval,
    occurrenceCount = (end as? End.AfterCount)?.count,
    // KMP gives UNTIL the type DTSTART requires, the end of the day keeps the occurrences of that day
    until = (end as? End.Until)?.let { RecurrenceUntil.Floating(it.date.atTime(23, 59, 59)) },
    byDay = when (frequency) {
        RecurrenceUi.Rule.Frequency.Daily -> emptyList()
        RecurrenceUi.Rule.Frequency.Weekly -> daysOfWeek.sorted().map { WeekDayNum(dayOfWeek = it) }
        RecurrenceUi.Rule.Frequency.Monthly, RecurrenceUi.Rule.Frequency.Yearly -> when (val monthDay = monthDay) {
            MonthDay.StartDay -> emptyList()
            is MonthDay.Weekday -> listOf(WeekDayNum(ordinal = monthDay.position, dayOfWeek = monthDay.dayOfWeek))
        }
    },
    byMonth = if (frequency == RecurrenceUi.Rule.Frequency.Yearly && monthDay is MonthDay.Weekday) {
        listOf(start.month.number)
    } else {
        emptyList()
    },
)
