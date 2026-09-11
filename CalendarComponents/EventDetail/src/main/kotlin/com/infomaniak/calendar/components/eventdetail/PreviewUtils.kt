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
package com.infomaniak.calendar.components.eventdetail

import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus

internal val previewAttendees = listOf(
    AttendeeUi(email = "alice@example.com", displayName = "Alice Johnson", status = ParticipationStatus.Accepted),
    AttendeeUi(email = "bob@example.com", displayName = "Bob Smith", status = ParticipationStatus.Tentative),
    AttendeeUi(email = "carla@example.com", displayName = "Carla Lopez", status = ParticipationStatus.Declined),
    AttendeeUi(email = "denis@example.com", displayName = "Denis Brown", status = ParticipationStatus.NeedsAction),
)
