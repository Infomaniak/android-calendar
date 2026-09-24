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
package com.infomaniak.calendar.components.attendeessearch.preview

import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus

val previewAttendees = listOf(
    AttendeeUi(
        email = "alice@example.com",
        displayName = "Alice Johnson",
        isOrganizer = true,
        status = ParticipationStatus.Accepted,
    ),
    AttendeeUi(
        email = "bob@example.com",
        displayName = "Bob Smith",
        status = ParticipationStatus.Tentative,
    ),
    AttendeeUi(
        email = "charlie@example.com",
        displayName = "Charlie Brown",
        status = ParticipationStatus.Declined,
    ),
)
