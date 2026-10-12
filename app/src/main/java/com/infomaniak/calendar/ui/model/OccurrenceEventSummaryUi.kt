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
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import kotlin.time.Instant

@Immutable
data class OccurrenceEventSummaryUi(
    override val id: String,
    val occurrenceId: OccurrenceId,
    override val title: String,
    override val location: String?,
    override val status: EventStatus,
    override val start: Instant,
    override val end: Instant,
    override val isAllDay: Boolean,
    override val colors: EventColorsUi,
    override val hasAttendees: Boolean,
    override val myStatus: ParticipationStatus?,
) : EventSummaryUi.Normal

/** All [EventSummaryUi.Normal] used in the app are [OccurrenceEventSummaryUi] and we need to cast it to get the occurrence id back */
val EventSummaryUi.Normal.occurrenceId: OccurrenceId get() = (this as OccurrenceEventSummaryUi).occurrenceId
