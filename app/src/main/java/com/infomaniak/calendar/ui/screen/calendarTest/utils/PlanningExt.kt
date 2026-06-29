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
package com.infomaniak.calendar.ui.screen.calendarTest.utils

import com.infomaniak.calendar.ui.screen.calendarTest.model.PlanningDayUi
import com.infomaniak.calendar.ui.screen.calendarTest.model.PlanningWeekUi
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val DAYS_IN_WEEK = 7

/**
 * Builds the week sections covering the [rangeStart, rangeEnd[ range. Every week in the range is
 * emitted (even empty ones) so the timeline never has gaps; each week lists only the days that
 * actually contain events. Each event is placed on its start day.
 */
@OptIn(ExperimentalTime::class)
internal fun List<Event>.toPlanningWeeks(rangeStart: Instant, rangeEnd: Instant): List<PlanningWeekUi> {
    // TODO: Timezones are not handled yet — days/weeks are computed in UTC.
    val timeZone = TimeZone.UTC
    val dayLabelFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    val dayHeaderFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())

    val eventsByDay = groupBy { event -> event.timing.start.toLocalDateTime(timeZone).date }

    val endDate = rangeEnd.toLocalDateTime(timeZone).date
    val weeks = mutableListOf<PlanningWeekUi>()
    var weekStart = rangeStart.toLocalDateTime(timeZone).date.startOfWeek()

    while (weekStart < endDate) {
        val weekEnd = weekStart.plus(DatePeriod(days = DAYS_IN_WEEK - 1))
        val days = (0 until DAYS_IN_WEEK).mapNotNull { dayOffset ->
            val date = weekStart.plus(DatePeriod(days = dayOffset))
            eventsByDay[date]?.takeIf { dayEvents -> dayEvents.isNotEmpty() }?.let { dayEvents ->
                PlanningDayUi(
                    id = date.toString(),
                    header = date.toJavaLocalDate().format(dayHeaderFormatter)
                        .replaceFirstChar { char -> char.uppercase() },
                    events = dayEvents.sortedBy { event -> event.timing.start }.map(Event::toUi),
                )
            }
        }

        weeks += PlanningWeekUi(
            id = weekStart.toString(),
            header = weekHeader(weekStart, weekEnd, dayLabelFormatter),
            days = days,
        )
        weekStart = weekStart.plus(DatePeriod(days = DAYS_IN_WEEK))
    }

    return weeks
}

/** e.g. "S12 2026 · 24 févr. - 2 mars" */
private fun weekHeader(weekStart: LocalDate, weekEnd: LocalDate, dayLabelFormatter: DateTimeFormatter): String {
    val javaWeekStart = weekStart.toJavaLocalDate()
    val weekNumber = javaWeekStart.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    val weekBasedYear = javaWeekStart.get(IsoFields.WEEK_BASED_YEAR)
    val from = javaWeekStart.format(dayLabelFormatter)
    val to = weekEnd.toJavaLocalDate().format(dayLabelFormatter)
    return "S$weekNumber $weekBasedYear · $from - $to"
}

/** Monday of the week containing this date (ISO week starts on Monday). */
private fun LocalDate.startOfWeek(): LocalDate = minus(DatePeriod(days = dayOfWeek.isoDayNumber - 1))


