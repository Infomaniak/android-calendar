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
package com.infomaniak.calendar.ui.screen.eventDetail.utils

import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.utils.formatUtc
import com.infomaniak.calendar.ui.utils.toTimeRange
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal fun Event.toDetailUiState(): EventDetailUiState.Loaded = EventDetailUiState.Loaded(
    eventId = id,
    title = title.ifBlank { "(no title)" },
    timeRange = timing.toTimeRange(),
    location = location.notBlank(),
    status = status.notBlank(),
    categories = categories.notBlank(),
    description = description.notBlank(),
    lastModified = lastModified?.formatUtc(),
    color = calendarColor.argb,
    canEdit = canEdit,
)

private fun String?.notBlank(): String? = this?.takeIf(String::isNotBlank)
