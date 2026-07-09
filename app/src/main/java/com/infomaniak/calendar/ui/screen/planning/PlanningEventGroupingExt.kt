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
import com.infomaniak.calendar.components.foundation.models.EventColorUi
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.models.YearWeek
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Attendee
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventColor
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventColors
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.SortedMap
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
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
 * Groups events by the [week][YearWeek] they fall in and then by their day.
 *
 * Events without a [start][EventTiming.start]
 * instant are ignored since they cannot be placed on a calendar day.
 *
 * Instants are bucketed using [timeZone] (the device's current timezone by default), and weeks are
 * resolved using [weekNumbering] (ISO-8601 by default).
 *
 * The nested sorted structure is filled in a single pass: each event is placed directly into its week
 * and day bucket, with no intermediate collections allocated along the way.
 */
@OptIn(ExperimentalTime::class)
suspend fun List<Event>.groupByWeekAndDay(
    emailsByUserId: Map<AccountId, String>,
    weekNumbering: WeekNumbering = WeekNumbering.ISO_8601,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): EventsByWeekAndDay = withContext(Dispatchers.Default) {
    val result = sortedMapOf<YearWeek, SortedMap<LocalDate, MutableList<EventUi>>>()

    for (event in this@groupByWeekAndDay) {
        ensureActive()
        val date = event.getStartAt(timeZone)
        result
            .getOrPut(weekNumbering.weekOf(date)) { sortedMapOf() }
            .getOrPut(date) { mutableListOf() }
            .add(event.toEventUi(emailsByUserId))
    }

    result.ensureTodayHasEntry(timeZone, weekNumbering)

    @Suppress("UNCHECKED_CAST") // Shows the exposed list as non-mutable
    return@withContext result as EventsByWeekAndDay
}

private fun SortedMap<YearWeek, SortedMap<LocalDate, MutableList<EventUi>>>.ensureTodayHasEntry(
    timeZone: TimeZone,
    weekNumbering: WeekNumbering,
) {
    val today = Clock.System.now().toLocalDateTime(timeZone).date
    val todayEvents = getOrPut(weekNumbering.weekOf(today)) { sortedMapOf() }.getOrPut(today) { mutableListOf() }

    if (todayEvents.isEmpty()) todayEvents.add(EventUi.TodayEmptyState)
}

// TODO: Handle AllDay
private fun Event.getStartAt(timeZone: TimeZone): LocalDate {
    return timing.startIn(timeZone).date
}

private fun Event.toEventUi(emailsByUserId: Map<AccountId, String>): EventUi = EventUi.Normal(
    id = id.url,
    title = title,
    location = location,
    status = status.toEventStatus(),
    start = timing.startInstantLocal(),
    end = timing.endInstantLocal(),
    isAllDay = timing.isAllDay,
    colors = colors.toEventColorsUi(),
    attendees = toAttendees(attendees, emailsByUserId),
)

private fun Event.toAttendees(attendees: List<Attendee>, emailsByUserId: Map<AccountId, String>): Attendees {
    val all = attendees.map(Attendee::toAttendeeUi)
    val me = all.find { it.email == emailsByUserId[accountId] }

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
    _datavizContainer = datavizContainer.toEventColorUi(),
    _onDatavizContainer = onDatavizContainer.toEventColorUi(),
    _datavizContainerVariant = datavizContainerVariant.toEventColorUi(),
    _onDatavizContainerVariant = onDatavizContainerVariant.toEventColorUi(),
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

private fun EventColor.toEventColorUi(): EventColorUi = EventColorUi(light = light, dark = dark)
