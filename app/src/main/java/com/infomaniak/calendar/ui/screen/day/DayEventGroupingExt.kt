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
package com.infomaniak.calendar.ui.screen.day

import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.model.TimedEvent
import com.infomaniak.calendar.components.day.model.toTimedEvent
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.utils.toEventUi
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventDaySlice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.coroutines.coroutineContext

typealias DayEventsByDate = Map<LocalDate, DayEvents>

/**
 * Splits already day-sliced events into what the day view draws in each of its two areas.
 *
 * A slice goes to the all-day band as soon as it covers the whole day: true all-day events, but also
 * the middle days of a timed event spanning several days, which have no meaningful hours to show on
 * the grid. Everything else becomes a [TimedEvent] positioned in minutes within its own day.
 */
suspend fun Map<LocalDate, List<EventDaySlice>>.toDayEventsByDate(
    emailsByUserId: Map<AccountId, String>,
    timeZone: TimeZone,
): DayEventsByDate = withContext(Dispatchers.Default) {
    mapValues { (date, slices) ->
        coroutineContext.ensureActive()
        slices.toDayEvents(date, emailsByUserId, timeZone)
    }
}

private fun List<EventDaySlice>.toDayEvents(
    date: LocalDate,
    emailsByUserId: Map<AccountId, String>,
    timeZone: TimeZone,
): DayEvents {
    val allDay = mutableListOf<EventUi.Normal>()
    val timed = mutableListOf<TimedEvent>()

    forEach { slice ->
        val event = slice.toEventUi(emailsByUserId, timeZone)
        if (slice.fillsWholeDay) allDay.add(event) else timed.add(event.toTimedEvent(date, timeZone))
    }

    return DayEvents(allDay = allDay, timed = timed)
}
