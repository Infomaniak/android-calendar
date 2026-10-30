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
package com.infomaniak.calendar.components.eventdetail.models

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Immutable
import com.infomaniak.calendar.components.resources.R
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import java.io.Serializable

@Immutable
sealed interface RecurrenceUi : Serializable {
    @Immutable
    data object None : RecurrenceUi

    /** A stored recurrence the form can't describe, kept as it is unless another one is picked. */
    @Immutable
    data object Kept : RecurrenceUi

    /**
     * @param daysOfWeek Only read by [Frequency.Weekly]; empty repeats on the start's day.
     * @param monthDay Only read by [Frequency.Monthly] and [Frequency.Yearly], the latter in the start's month.
     */
    @Immutable
    data class Rule(
        val frequency: Frequency,
        val interval: Int,
        val daysOfWeek: Set<DayOfWeek>,
        val monthDay: MonthDay,
        val end: End,
    ) : RecurrenceUi {
        @Immutable
        enum class Frequency(@PluralsRes val unitRes: Int) {
            Daily(R.plurals.unitDays),
            Weekly(R.plurals.unitWeeks),
            Monthly(R.plurals.unitMonths),
            Yearly(R.plurals.unitYears),
        }

        @Immutable
        sealed interface MonthDay : Serializable {
            @Immutable
            data object StartDay : MonthDay

            /** @param position From 1 to 4, or -1 for the last [dayOfWeek] of the month. */
            @Immutable
            data class Weekday(val position: Int, val dayOfWeek: DayOfWeek) : MonthDay
        }

        @Immutable
        sealed interface End : Serializable {
            @Immutable
            data object Never : End

            @Immutable
            data class AfterCount(val count: Int) : End

            @Immutable
            data class Until(val date: LocalDate) : End
        }

        companion object {
            val presets = Frequency.entries.map { frequency ->
                Rule(frequency, interval = 1, daysOfWeek = emptySet(), monthDay = MonthDay.StartDay, end = End.Never)
            }
        }
    }
}
