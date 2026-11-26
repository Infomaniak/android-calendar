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
package com.infomaniak.calendar.components.days.component

import androidx.compose.runtime.Composable
import com.infomaniak.calendar.components.days.model.EventMove
import com.infomaniak.calendar.components.event.highlight.EventHighlightState
import com.infomaniak.calendar.components.event.highlight.rememberEventHighlightState
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import kotlinx.datetime.atTime

/**
 * The highlight state of a day timeline, reporting a drop on an hour grid, which tells a time, as an
 * [EventMove.Timed], and one on an all-day band, which doesn't, as an [EventMove.AllDay], unless the event
 * already starts there.
 */
@Composable
internal fun rememberDayHighlightState(onEventMoved: ((EventSummaryUi.Normal, EventMove) -> Unit)?): EventHighlightState {
    return rememberEventHighlightState(
        onEventDropped = onEventMoved?.let { moved ->
            { event, drop ->
                val time = drop.time
                val eventStart = event.eventStart
                val move = when {
                    time == null -> EventMove.AllDay(startDate = drop.date)
                        .takeUnless { event.isAllDay && drop.date == eventStart?.date }
                    else -> EventMove.Timed(start = drop.date.atTime(time))
                        .takeUnless { !event.isAllDay && it.start == eventStart }
                }
                move?.let { moved(event, it) }
            }
        },
    )
}
