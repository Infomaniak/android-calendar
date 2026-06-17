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
package com.infomaniak.calendar.components.foundation.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.time.temporal.WeekFields

private const val DAYS_IN_WEEK = 7

/**
 * A single calendar week, identified by its [firstDay], together with the [weekNumber] it is labelled
 * with under a given [WeekNumbering] scheme.
 *
 * The class exposes everything needed to render a week header such as
 * `"Week 15 - 20 - 26 December 2029"`: the [weekNumber], the [firstDay] and [lastDay] of the week, and
 * the [month]/[year] the week falls in. A week may straddle two months; [firstDay] and [lastDay] then
 * carry each end while [month]/[year] follow the [firstDay].
 *
 * Identity (equality and ordering) is based on [firstDay], so two [YearWeek] instances produced by the
 * same [WeekNumbering] for the same week are interchangeable as map keys. Build instances via
 * [WeekNumbering.weekOf] rather than the constructor.
 */
@Parcelize
data class YearWeek(val firstDay: LocalDate, val weekNumber: Int) : Parcelable, Comparable<YearWeek> {

    /** Last day of the week, six days after [firstDay]. */
    val lastDay: LocalDate get() = firstDay.plus(DatePeriod(days = DAYS_IN_WEEK - 1))

    /** Month the week starts in. See [lastDay] for the other end when the week spans two months. */
    val month: Month get() = firstDay.month

    /** Calendar year the week starts in. */
    val year: Int get() = firstDay.year

    override fun compareTo(other: YearWeek): Int = firstDay.compareTo(other.firstDay)
}

/**
 * Describes how calendar weeks are numbered and which weekday a week starts on.
 *
 * Only supports ISO_8601 for now, but news week numbering can be instantiated by defining other [WeekFields] as needed.
 *
 * The Java [LocalDate] is needed to provide this [WeekFields] customization.
 */
class WeekNumbering private constructor(internal val weekFields: WeekFields) {

    /** Returns the [YearWeek] that [date] belongs to under this numbering scheme. */
    fun weekOf(date: LocalDate): YearWeek {
        val javaDate = date.toJavaLocalDate()
        return YearWeek(
            firstDay = javaDate.with(weekFields.dayOfWeek(), 1L).toKotlinLocalDate(),
            weekNumber = javaDate.get(weekFields.weekOfWeekBasedYear()),
        )
    }

    companion object {
        /**
         * ISO-8601 numbering: weeks start on Monday and week 1 is the first week with at least 4 of its
         * days in the new year — i.e. the week containing the first Thursday of January.
         */
        val ISO_8601: WeekNumbering = WeekNumbering(WeekFields.ISO)
    }
}

object LocalDateParceler : Parceler<LocalDate> {
    override fun create(parcel: Parcel): LocalDate = LocalDate.parse(parcel.readString()!!)
    override fun LocalDate.write(parcel: Parcel, flags: Int) = parcel.writeString(toString())
}
