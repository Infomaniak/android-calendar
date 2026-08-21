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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventStatus as KmpEventStatus
import com.infomaniak.multiplatform_calendar.core.domain.model.event.ParticipationStatus as KmpParticipationStatus

/**
 * Groups the day-split events of a *single* [week] by day, filling **every** day of the week so the
 * planning always shows all seven days — empty days get a single [EventUi.EmptyState] placeholder
 * ([EventUi.TodayEmptyState] for today). Ready to be flattened into one Paging page via `planningRows`.
 *
 * The input is expected to only contain days belonging to [week] (as returned by
 * `CalendarManager.observeDaySlices` queried over that week's range), already sorted within each day
 * (all-day first, then by start time).
 */
@OptIn(ExperimentalTime::class)
suspend fun Map<LocalDate, List<EventDaySlice>>.groupWeekDays(
    week: YearWeek,
    emailsByUserId: Map<AccountId, String>,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Map<LocalDate, List<EventUi>> = withContext(Dispatchers.Default) {
    val days = linkedMapOf<LocalDate, List<EventUi>>()

    var date = week.firstDay
    while (date <= week.lastDay) {
        ensureActive()
        val events = this@groupWeekDays[date]?.map { it.toEventUi(emailsByUserId, timeZone) }
        days[date] = events?.takeIf { it.isNotEmpty() } ?: listOf(emptyStateFor(date, timeZone))
        date = date.plus(DatePeriod(days = 1))
    }

    return@withContext days
}

@OptIn(ExperimentalTime::class)
private fun emptyStateFor(date: LocalDate, timeZone: TimeZone): EventUi {
    return if (date == Clock.today(timeZone)) EventUi.TodayEmptyState else EventUi.EmptyState(date)
}

@OptIn(ExperimentalTime::class)
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
    _sourceColor = sourceColor,
    _containerColor = containerColor,
    _onContainerColor = onContainerColor.toThemedColorUi(),
    _containerVariantColor = containerVariantColor,
    _onContainerVariantColor = onContainerVariantColor.toThemedColorUi(),
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

