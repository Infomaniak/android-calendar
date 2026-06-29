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
package com.infomaniak.calendar.ui.screen.calendarTest.model

import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarColor

/**
 * UI model for an event shown in the CalDAV test screen: everything is already formatted/derived
 * so the composables only have to render plain strings (no date formatting, no business logic).
 */
data class EventUi(
    val id: EventId,
    val title: String,
    val timeRange: String?,
    val location: String?,
    val status: String?,
    val categories: String?,
    val description: String?,
    val lastModified: String?,
    val calendarColor: CalendarColor,
    val canEdit: Boolean,
)
