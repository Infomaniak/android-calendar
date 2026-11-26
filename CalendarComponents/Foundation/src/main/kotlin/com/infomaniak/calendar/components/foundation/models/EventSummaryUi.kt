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
package com.infomaniak.calendar.components.foundation.models

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Immutable
sealed interface EventSummaryUi {
    val id: String

    @Immutable
    interface Normal : EventSummaryUi {
        /** The event this shows, shared by the pieces of one shown over several days, each with its own [id]. */
        val eventId: String get() = id
        val title: String
        val location: String?
        val status: EventStatus
        val start: Instant
        val end: Instant
        val isAllDay: Boolean
        /**
         * When the whole event starts and ends, on the calendar's clock, while [start] and [end] are this piece's.
         * This piece's by default, then standing for the whole event.
         */
        val eventStart: LocalDateTime get() = start.toLocalDateTime(TimeZone.currentSystemDefault())
        val eventEnd: LocalDateTime get() = end.toLocalDateTime(TimeZone.currentSystemDefault())
        val colors: EventColorsUi
        val hasAttendees: Boolean
        val myStatus: ParticipationStatus?
    }

    @Immutable
    data object TodayEmptyState : EventSummaryUi {
        override val id: String = TODAY_EMPTY_STATE_ID
    }

    @Immutable
    data class EmptyState(val date: LocalDate) : EventSummaryUi {
        override val id: String = "${date}_$EMPTY_STATE_ID"
    }

    companion object {
        private const val TODAY_EMPTY_STATE_ID = "todayEmptyState"
        private const val EMPTY_STATE_ID = "emptyState"
    }
}

enum class EventStatus {
    Confirmed,
    Tentative,
    Cancelled,
}
