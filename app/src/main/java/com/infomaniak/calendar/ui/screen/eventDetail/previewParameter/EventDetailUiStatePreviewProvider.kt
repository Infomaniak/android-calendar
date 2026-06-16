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
package com.infomaniak.calendar.ui.screen.eventDetail.previewParameter

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId

internal class EventDetailUiStatePreviewProvider : PreviewParameterProvider<EventDetailUiState> {
    override val values = sequenceOf(EventDetailUiState.Loading, Loaded, Error)

    companion object {
        val Loaded = EventDetailUiState.Loaded(
            eventId = EventId("https://example.com/calendars/default/event-1.ics"),
            title = "Weekly sync",
            timeRange = "10:00 → 11:00",
            location = "Meeting room A",
            status = "CONFIRMED",
            categories = "Work",
            description = "Standup and planning session with the full team.",
            lastModified = "2026-01-03 09:55",
            color = 0xFF2196F3.toInt(),
            canEdit = true,
        )

        val Error = EventDetailUiState.Error("Impossible de supprimer l'événement.")
    }
}



