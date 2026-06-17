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
package com.infomaniak.calendar.ui.screen.eventFormTest.model

import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import kotlinx.datetime.LocalDateTime

/** Editable form values. Times are wall-clock UTC for now (timezones are a TODO). */
data class EventFormData(
    val title: String,
    val isAllDay: Boolean,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val location: String,
    val description: String,
    val calendarId: CalendarId,
)
/** A calendar the event can be assigned to. */
data class CalendarChoice(
    val id: CalendarId,
    val name: String,
    val color: Int,
)

