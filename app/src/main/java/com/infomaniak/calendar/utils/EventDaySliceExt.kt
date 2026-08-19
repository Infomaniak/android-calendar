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

import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.ui.screen.planning.toEventColorsUi
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Attendee
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventColors
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventDaySlice
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventStatus as KmpEventStatus
import com.infomaniak.multiplatform_calendar.core.domain.model.event.ParticipationStatus as KmpParticipationStatus

/**
 * Translation of the KMP event model into the UI model consumed by CalendarComponents, shared by
 * every calendar view so they all render the same event identically.
 *
 * The resulting [EventUi.Normal] carries the slice's own day-clamped bounds, converted to an
 * absolute instant in [timeZone].
 */
fun EventDaySlice.toEventUi(emailsByUserId: Map<AccountId, String>, timeZone: TimeZone): EventUi.Normal = EventUi.Normal(
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
