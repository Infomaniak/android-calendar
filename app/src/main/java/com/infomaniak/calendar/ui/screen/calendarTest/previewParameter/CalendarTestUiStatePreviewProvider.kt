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
import com.infomaniak.calendar.ui.screen.calendarTest.model.EventUi
import com.infomaniak.calendar.ui.screen.calendarTest.model.PlanningDayUi
import com.infomaniak.calendar.ui.screen.calendarTest.model.PlanningWeekUi
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId

internal class CalendarTestUiStatePreviewProvider : PreviewParameterProvider<CalendarTestUiState> {
    override val values = sequenceOf(CalendarTestUiState.Loading, Loaded, Error)

    companion object {
        val EventUi = EventUi(
            id = EventId("event-preview"),
            title = "Weekly sync",
            timeRange = "10:00 → 11:00",
            location = "Meeting room A",
            status = "CONFIRMED",
            categories = "Work",
            description = "Standup and planning",
            lastModified = "03/01/2026 09:55",
            color = 0xFF2196F3.toInt(),
            canEdit = true,
        )

        private val dayWithEvents = PlanningDayUi(
            id = "2026-01-02",
            header = "Vendredi 2 janvier",
            events = listOf(
                EventUi,
                EventUi.copy(
                    id = EventId("event-preview-2"),
                    title = "Lunch with team",
                    timeRange = "12:30 → 13:30",
                    color = 0xFFE91E63.toInt(),
                    canEdit = false,
                ),
            ),
        )

        val Loaded = CalendarTestUiState.Loaded(
            weeks = listOf(
                PlanningWeekUi(
                    id = "2025-12-29",
                    header = "S1 2026 · 29 déc. - 4 janv.",
                    days = listOf(dayWithEvents),
                ),
                PlanningWeekUi(
                    id = "2026-01-05",
                    header = "S2 2026 · 5 janv. - 11 janv.",
                    days = emptyList(),
                ),
                PlanningWeekUi(
                    id = "2026-01-12",
                    header = "S3 2026 · 12 janv. - 18 janv.",
                    days = listOf(
                        PlanningDayUi(
                            id = "2026-01-14",
                            header = "Mercredi 14 janvier",
                            events = listOf(
                                EventUi.copy(
                                    id = EventId("event-preview-3"),
                                    title = "Hiking",
                                    timeRange = "All day",
                                    color = 0xFF4CAF50.toInt(),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val Error = CalendarTestUiState.Error("Something went wrong")
    }
}
