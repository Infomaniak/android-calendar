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
package com.infomaniak.calendar.ui.screen.planning

import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.utils.toEventUi
import com.infomaniak.calendar.utils.toThemedColorUi
import com.infomaniak.core.common.utils.today
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventColors
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventDaySlice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.time.Clock

/** Every day of [days] with its events, or a single empty-state placeholder when it has none. */
suspend fun Map<LocalDate, List<EventDaySlice>>.groupByDay(
    days: ClosedRange<LocalDate>,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Map<LocalDate, List<EventSummaryUi>> = withContext(Dispatchers.Default) {
    val eventsByDay = HashMap<LocalDate, List<EventSummaryUi>>()

    var date = days.start
    while (date <= days.endInclusive) {
        ensureActive()
        val events = this@groupByDay[date]?.map { it.toEventUi(timeZone) }
        eventsByDay[date] = events?.takeIf { it.isNotEmpty() } ?: listOf(emptyStateFor(date, timeZone))
        date = date.plus(DatePeriod(days = 1))
    }

    return@withContext eventsByDay
}

private fun emptyStateFor(date: LocalDate, timeZone: TimeZone): EventSummaryUi {
    return if (date == Clock.today(timeZone)) EventSummaryUi.TodayEmptyState else EventSummaryUi.EmptyState(date)
}

fun EventColors.toEventColorsUi(): EventColorsUi = EventColorsUi(
    _calendarSourceColor = calendarSourceColor.argb,
    _sourceColor = sourceColor,
    _containerColor = containerColor,
    _onContainerColor = onContainerColor.toThemedColorUi(),
    _containerVariantColor = containerVariantColor,
    _onContainerVariantColor = onContainerVariantColor.toThemedColorUi(),
)
