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
package com.infomaniak.calendar.components.day.preview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.model.TimedEvent
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.preview.EventColorsUiFactory
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Clock

private val previewTimeZone = TimeZone.currentSystemDefault()

private val acceptedByMe = Attendees(
    all = listOf(AttendeeUi("alice@example.com", "Alice", ParticipationStatus.Accepted)),
    me = AttendeeUi("alice@example.com", "Alice", ParticipationStatus.Accepted),
)

private val declinedByMe = Attendees(
    all = listOf(AttendeeUi("alice@example.com", "Alice", ParticipationStatus.Declined)),
    me = AttendeeUi("alice@example.com", "Alice", ParticipationStatus.Declined),
)

internal val previewDayEvents: DayEvents by lazy {
    DayEvents(
        allDay = listOf(
            previewEvent("Tennis", 0, 0, 0, 0, isAllDay = true, color = Color(0xFFE5B94E)),
            previewEvent("Anniversaire de Léa", 0, 0, 0, 0, isAllDay = true, color = Color(0xFFD4426E)),
            previewEvent("Réveillon", 0, 0, 0, 0, isAllDay = true, color = Color(0xFF8B5CF6)),
            previewEvent("Faire les courses", 0, 0, 0, 0, isAllDay = true, color = Color(0xFF3B82F6)),
        ),
        timed = listOf(
            previewTimedEvent("Aller courir au bord du lac", 7, 0, 8, 0, "Ouchy-Olympique", Color(0xFF3B82F6)),
            previewTimedEvent("Meeting dev", 8, 5, 8, 35, null, Color(0xFFD4426E), attendees = declinedByMe),
            previewTimedEvent("Revue roadmap app Drive", 9, 0, 11, 0, "Salle American Diner", Color(0xFF8B5CF6)),
            previewTimedEvent("Revue design Calendar", 11, 15, 12, 15, "Salle Hard Rock", Color(0xFFE5B94E)),
            previewTimedEvent("Revue design kSuite", 11, 15, 12, 15, "Salle Hard Rock", Color(0xFF3B82F6), declinedByMe),
            previewTimedEvent("Revue design kMeet", 11, 45, 12, 45, "Salle Hard Rock", Color(0xFFD4426E)),
            previewTimedEvent("Repas du mois avec l'équipe", 13, 0, 14, 0, "L'Atelier Robuchon", Color(0xFF10B981)),
        ),
    )
}

private fun previewTimedEvent(
    title: String,
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    location: String?,
    color: Color,
    attendees: Attendees = acceptedByMe,
): TimedEvent = TimedEvent(
    event = previewEvent(title, startHour, startMinute, endHour, endMinute, false, color, location, attendees),
    startMinuteOfDay = startHour * 60 + startMinute,
    endMinuteOfDay = endHour * 60 + endMinute,
)

private fun previewEvent(
    title: String,
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    isAllDay: Boolean,
    color: Color,
    location: String? = null,
    attendees: Attendees = acceptedByMe,
): EventUi.Normal {
    val today = Clock.today(previewTimeZone)

    return EventUi.Normal(
        id = "$title-$startHour:$startMinute",
        masterEventId = "$title-$startHour:$startMinute",
        title = title,
        location = location,
        status = EventStatus.Confirmed,
        start = LocalDateTime(today.year, today.month, today.day, startHour, startMinute).toInstant(previewTimeZone),
        end = LocalDateTime(today.year, today.month, today.day, endHour, endMinute).toInstant(previewTimeZone),
        isAllDay = isAllDay,
        colors = EventColorsUiFactory.dummyEventColorsUiFactory.create(color.toArgb()),
        attendees = attendees,
    )
}
