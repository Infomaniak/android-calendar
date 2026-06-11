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
package com.infomaniak.calendar.ui.screen.calendarTest.previewParameter

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.infomaniak.calendar.ui.screen.calendarTest.CalendarTestUiState
import com.infomaniak.calendar.ui.screen.calendarTest.model.CalendarUi
import com.infomaniak.calendar.ui.screen.calendarTest.model.EventUi

internal class CalendarTestUiStatePreviewProvider : PreviewParameterProvider<CalendarTestUiState> {
    override val values = sequenceOf(CalendarTestUiState.Loading, Loaded, Error)

    companion object {
        val EventUi = EventUi(
            id = "event-preview",
            title = "Weekly sync",
            timeRange = "03/01/2026 10:00 → 03/01/2026 11:00",
            location = "Meeting room A",
            status = "CONFIRMED",
            categories = "Work",
            description = "Standup and planning",
            lastModified = "03/01/2026 09:55",
        )

        val CalendarUi = CalendarUi(
            id = "calendar-preview",
            header = "Work · 1 event(s)",
            color = 0xFF2196F3.toInt(),
            isReadOnly = false,
            events = List(3) { EventUi.copy(id = "$it") },
        )

        val Loaded = CalendarTestUiState.Loaded(
            listOf(
                CalendarUi,
                CalendarUi.copy(
                    id = "calendar-preview-ro",
                    header = "Team (shared) · 2 event(s)",
                    color = 0xFFE91E63.toInt(),
                    isReadOnly = true,
                ),
                CalendarUi.copy(
                    id = "calendar-preview-empty",
                    header = "Holidays · 0 event(s)",
                    color = 0xFF4CAF50.toInt(),
                    events = emptyList(),
                ),
            ),
        )

        val Error = CalendarTestUiState.Error("Something went wrong")
    }
}
