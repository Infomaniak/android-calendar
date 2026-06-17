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
package com.infomaniak.calendar.ui.screen.eventFormTest

import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import kotlinx.datetime.LocalDateTime

sealed interface EventFormAction {
    data class OnTitleChange(val title: String) : EventFormAction
    data class OnAllDayChange(val isAllDay: Boolean) : EventFormAction
    data class OnStartChange(val start: LocalDateTime) : EventFormAction
    data class OnEndChange(val end: LocalDateTime) : EventFormAction
    data class OnLocationChange(val location: String) : EventFormAction
    data class OnDescriptionChange(val description: String) : EventFormAction
    data class OnCalendarChange(val calendarId: CalendarId) : EventFormAction
    data object OnClickSave : EventFormAction
    data object OnClickBack : EventFormAction
}

