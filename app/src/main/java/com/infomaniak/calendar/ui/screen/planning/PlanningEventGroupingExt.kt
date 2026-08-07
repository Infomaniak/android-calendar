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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import java.util.SortedMap
import kotlin.time.Clock
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
@Suppress("UNCHECKED_CAST") // Shows the exposed list as non-mutable
suspend fun Map<LocalDate, List<EventDaySlice>>.groupByWeekAndDay(
    emailsByUserId: Map<AccountId, String>,
    weekNumbering: WeekNumbering = WeekNumbering.ISO_8601, //TODO[weekNumbering]: Use week numbering from LocalSettings
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): EventsByWeekAndDay = withContext(Dispatchers.Default) {
    val eventsByWeekAndDay = sortedMapOf<YearWeek, SortedMap<LocalDate, MutableList<EventUi>>>()

    val firstDate = keys.firstOrNull() ?: return@withContext eventsByWeekAndDay as EventsByWeekAndDay
    val lastDate = keys.lastOrNull() ?: return@withContext eventsByWeekAndDay as EventsByWeekAndDay

    for (week in weekNumbering.weeksBetween(firstDate, lastDate)) {
        ensureActive()
        val days = eventsByWeekAndDay.getOrPut(week) { sortedMapOf() }
        var date = week.firstDay
        while (date <= week.lastDay) {
            days[date] = this@groupByWeekAndDay[date]
                ?.mapTo(mutableListOf()) { it.toEventUi(emailsByUserId, timeZone) }
                .ensureHasEntry(date, timeZone)
            date = date.plus(DatePeriod(days = 1))
        }
    }

    return@withContext eventsByWeekAndDay as EventsByWeekAndDay
}

private fun MutableList<EventUi>?.ensureHasEntry(date: LocalDate, timeZone: TimeZone): MutableList<EventUi> {
    val events = this ?: mutableListOf()
    if (events.isEmpty()) events.add(if (date == Clock.today(timeZone)) EventUi.TodayEmptyState else EventUi.EmptyState(date))
    return events
}

private fun EventDaySlice.toEventUi(emailsByUserId: Map<AccountId, String>, timeZone: TimeZone): EventUi = EventUi.Normal(
    id = "${event.occurrenceId.value}@$date",
    masterEventId = event.masterEventId.url,
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
