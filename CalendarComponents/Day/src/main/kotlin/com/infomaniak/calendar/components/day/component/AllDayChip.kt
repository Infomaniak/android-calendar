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
package com.infomaniak.calendar.components.day.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.infomaniak.calendar.components.event.EventItem
import com.infomaniak.calendar.components.event.highlight.EventHighlightState
import com.infomaniak.calendar.components.event.highlight.highlightable
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi

/**
 * An all-day event's chip. With a [highlightState], a long press holds it up to drag it, [highlightKey]
 * telling apart the chips of an event shown more than once.
 */
@Composable
internal fun AllDayChip(
    event: EventSummaryUi.Normal,
    onClick: () -> Unit,
    highlightState: EventHighlightState?,
    modifier: Modifier = Modifier,
    textOverflow: TextOverflow = TextOverflow.Ellipsis,
    highlightKey: Any = event,
) {
    EventItem(
        event = event,
        onClick = onClick,
        allDayTextOverflow = textOverflow,
        modifier = modifier.then(
            highlightState?.let {
                Modifier.highlightable(it, event, key = highlightKey) { draggedModifier ->
                    EventItem(event = event, onClick = {}, allDayTextOverflow = textOverflow, modifier = draggedModifier)
                }
            } ?: Modifier,
        ),
    )
}
