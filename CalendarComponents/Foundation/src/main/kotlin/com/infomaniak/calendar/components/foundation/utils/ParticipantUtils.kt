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
package com.infomaniak.calendar.components.foundation.utils

import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.OrganizerSource
import com.infomaniak.calendar.components.foundation.models.ParticipantUi

/** Adds [attendee] to the participants of an event organized as [organizerSource], as [ParticipantUi.Me] when it is us. */
fun MutableSet<ParticipantUi>.addParticipant(attendee: AttendeeUi, organizerSource: OrganizerSource?) {
    val me = (organizerSource as? OrganizerSource.Dynamic)?.owner

    // Only add Me when creating a new event, which we organize
    if (isEmpty() && organizerSource is OrganizerSource.Dynamic) {
        add(ParticipantUi.Me)
    }
    add(if (attendee.email.equals(me?.email, ignoreCase = true)) ParticipantUi.Me else attendee)
}
