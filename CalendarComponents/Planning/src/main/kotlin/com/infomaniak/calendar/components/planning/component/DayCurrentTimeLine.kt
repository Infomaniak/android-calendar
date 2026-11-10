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
package com.infomaniak.calendar.components.planning.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.offset
import com.infomaniak.calendar.components.foundation.component.CurrentTimeLine
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlin.time.Instant

/**
 * Before the ongoing event that started last, otherwise before the next one to start, otherwise after the last one.
 * `null` without any timed event. Expects all-day events first, then timed ones by start.
 */
internal fun List<EventSummaryUi>.currentTimeIndex(now: Instant): Int? {
    val timedEvents = withIndex().mapNotNull { (index, event) ->
        (event as? EventSummaryUi.Normal)?.takeUnless { it.isAllDay }?.let { IndexedValue(index, it) }
    }
    if (timedEvents.isEmpty()) return null

    val ongoing = timedEvents
        .filter { (_, event) -> event.start <= now && now < event.end }
        .maxByOrNull { (_, event) -> event.start }
    val upcoming = timedEvents.firstOrNull { (_, event) -> event.start > now }
    return ongoing?.index ?: upcoming?.index ?: (timedEvents.last().index + 1)
}

@Composable
internal fun DayCurrentTimeLine() {
    CurrentTimeLine(
        modifier = Modifier
            .overhangStart(EsdsTheme.spacing.md)
            .fillMaxWidth()
            .height(EsdsTheme.spacing.md),
    )
}

/** Widens the content by [width] towards the start, past the bounds it is given. */
private fun Modifier.overhangStart(width: Dp) = layout { measurable, constraints ->
    val overhang = width.roundToPx()
    val placeable = measurable.measure(constraints.offset(horizontal = overhang))
    layout(placeable.width - overhang, placeable.height) { placeable.placeRelative(-overhang, 0) }
}
