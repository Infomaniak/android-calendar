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

import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.models.YearWeek
import com.infomaniak.calendar.utils.toThemedColorUi
import com.infomaniak.core.common.utils.today
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Attendee
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventColors
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventDaySlice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.util.SortedMap
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventStatus as KmpEventStatus
import com.infomaniak.multiplatform_calendar.core.domain.model.event.ParticipationStatus as KmpParticipationStatus

/**
 * Events grouped by week, then by day.
 *
 * The outer map is keyed by [YearWeek] and the inner map by the day's [LocalDate], both sorted
 * ascending so the result can be consumed directly by a calendar UI. Days are keyed by full date
 * (not day-of-month) so a week that straddles two months stays in chronological order.
 */
typealias EventsByWeekAndDay = SortedMap<YearWeek, SortedMap<LocalDate, List<EventUi>>>

/**
 * Groups already day-split events ([EventDaySlice]s, keyed by day) by the [week][YearWeek] they fall
 * in, then by their day, ready to be consumed by the planning UI.
 *
 * The input is expected to come from `CalendarManager.observeDaySlices`, i.e. already grouped by day
 * and sorted within each day (all-day first, then by start time). A multi-day event therefore yields
 * one [EventUi] per day it covers, each carrying that day's clamped [start][EventUi.Normal.start] /
 * [end][EventUi.Normal.end] bounds (converted to an absolute instant in [timeZone]).
 *
 * Weeks are resolved using [weekNumbering] (ISO-8601 by default). Today is guaranteed to have an
 * entry: an empty day gets a single [EventUi.TodayEmptyState] placeholder.
 */
@OptIn(ExperimentalTime::class)
suspend fun Map<LocalDate, List<EventDaySlice>>.groupByWeekAndDay(
    emailsByUserId: Map<AccountId, String>,
    weekNumbering: WeekNumbering = WeekNumbering.ISO_8601,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): EventsByWeekAndDay = withContext(Dispatchers.Default) {
    val eventsByWeekAndDay = sortedMapOf<YearWeek, SortedMap<LocalDate, MutableList<EventUi>>>()

    for ((date, daySlices) in this@groupByWeekAndDay) {
        ensureActive()
        val week = eventsByWeekAndDay.getOrPut(weekNumbering.weekOf(date)) { sortedMapOf() }
        week[date] = daySlices.mapTo(mutableListOf()) { slice -> slice.toEventUi(emailsByUserId, timeZone) }
    }

    eventsByWeekAndDay.ensureTodayHasEntry(timeZone, weekNumbering)

    @Suppress("UNCHECKED_CAST") // Shows the exposed list as non-mutable
    return@withContext eventsByWeekAndDay as EventsByWeekAndDay
}

private fun SortedMap<YearWeek, SortedMap<LocalDate, MutableList<EventUi>>>.ensureTodayHasEntry(
    timeZone: TimeZone,
    weekNumbering: WeekNumbering,
) {
    val todayEvents = getOrPut(weekNumbering.weekOf(Clock.today(timeZone))) { sortedMapOf() }.getOrPut(Clock.today()) { mutableListOf() }

    if (todayEvents.isEmpty()) todayEvents.add(EventUi.TodayEmptyState)
}

private fun EventDaySlice.toEventUi(emailsByUserId: Map<AccountId, String>, timeZone: TimeZone): EventUi = EventUi.Normal(
    id = "${event.id.url}@$date",
    title = event.title,
    location = event.location,
    status = event.status.toEventStatus(),
    start = displayStart.toInstant(timeZone),
    end = displayEnd.toInstant(timeZone),
    isAllDay = isAllDay,
    colors = event.colors.toEventColorsUi(),
    attendees = toAttendees(event.attendees, emailsByUserId),
)

private fun EventDaySlice.toAttendees(attendees: List<Attendee>, emailsByUserId: Map<AccountId, String>): Attendees {
    val all = attendees.map(Attendee::toAttendeeUi)
    val me = all.find { it.email == emailsByUserId[event.accountId] }

    return Attendees(all, me)
}

private fun KmpEventStatus?.toEventStatus(): EventStatus {
    return when (this) {
        KmpEventStatus.TENTATIVE -> EventStatus.Tentative
        KmpEventStatus.CANCELLED -> EventStatus.Cancelled
        else -> EventStatus.Confirmed
    }
}

fun EventColors.toEventColorsUi(): EventColorsUi = EventColorsUi(
    _datavizContainer = datavizContainer.toThemedColorUi(),
    _onDatavizContainer = onDatavizContainer.toThemedColorUi(),
    _datavizContainerVariant = datavizContainerVariant.toThemedColorUi(),
    _onDatavizContainerVariant = onDatavizContainerVariant.toThemedColorUi(),
)

private fun Attendee.toAttendeeUi(): AttendeeUi = AttendeeUi(
    email = email,
    displayName = displayName,
    status = status.toParticipationStatus(),
)

private fun KmpParticipationStatus.toParticipationStatus(): ParticipationStatus = when (this) {
    KmpParticipationStatus.Accepted -> ParticipationStatus.Accepted
    KmpParticipationStatus.Declined -> ParticipationStatus.Declined
    KmpParticipationStatus.Tentative -> ParticipationStatus.Tentative
    KmpParticipationStatus.NeedsAction -> ParticipationStatus.NeedsAction
}

fun EventsByWeekAndDay.indexOf(date: LocalDate): Int {
    var index = 0

    entries.forEach { (week, days) ->
        if (date < week.firstDay) return index
        index++ // Count the week header

        if (date <= week.lastDay) { // If the day is somewhere inside this week
            days.forEach { (day, events) ->
                if (day >= date) return index
                index += events.size
            }

            return index
        }

        index += days.values.sumOf { it.size }
    }

    return index
}

/**
 * The soonest upcoming event(s): the single next event, or several when they share the earliest
 * start time. Temporary stand-in for logic that will move to KMP.
 */
fun EventsByWeekAndDay.findNextEvents(now: Instant): List<EventUi.Normal> {
    val upcoming = values.asSequence()
        .flatMap { days -> days.values.asSequence().flatten() }
        .filterIsInstance<EventUi.Normal>()
        .filter { it.start >= now }
        .toList()

    val nextStart = upcoming.minOfOrNull { it.start } ?: return emptyList()
    return upcoming.filter { it.start == nextStart }
}
