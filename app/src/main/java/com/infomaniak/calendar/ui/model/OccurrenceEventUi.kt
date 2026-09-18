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
package com.infomaniak.calendar.ui.model

import androidx.compose.runtime.Immutable
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import kotlin.time.Instant

/**
 * The [EventUi.Normal] every calendar view is given, carrying the [occurrenceId] of the occurrence it displays.
 *
 * CalendarComponents never reads [occurrenceId]: it only hands the event back when it is clicked, which is where we
 * read it to open the detail of the very occurrence the user tapped. This is what keeps the KMP [OccurrenceId] out
 * of those modules entirely.
 */
@Immutable
data class OccurrenceEventUi(
    override val id: String,
    /** The displayed occurrence, never the master event of the series it belongs to. */
    val occurrenceId: OccurrenceId,
    override val title: String,
    override val location: String?,
    override val status: EventStatus,
    override val start: Instant,
    override val end: Instant,
    override val isAllDay: Boolean,
    override val colors: EventColorsUi,
    override val attendees: Attendees,
) : EventUi.Normal

/** The occurrence [this] displays, every event handed to a calendar view being an [OccurrenceEventUi]. */
val EventUi.Normal.occurrenceId: OccurrenceId get() = (this as OccurrenceEventUi).occurrenceId
