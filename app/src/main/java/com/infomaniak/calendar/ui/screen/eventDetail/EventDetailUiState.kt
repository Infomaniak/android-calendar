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
package com.infomaniak.calendar.ui.screen.eventDetail

import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId

sealed interface EventDetailUiState {

    data object Loading : EventDetailUiState

    data class Loaded(
        val eventId: EventId,
        val title: String,
        val timeRange: String?,
        val location: String?,
        val status: String?,
        val categories: String?,
        val description: String?,
        val lastModified: String?,
        val color: Int,
        val canEdit: Boolean,
    ) : EventDetailUiState

    data class Error(val message: String) : EventDetailUiState
}

