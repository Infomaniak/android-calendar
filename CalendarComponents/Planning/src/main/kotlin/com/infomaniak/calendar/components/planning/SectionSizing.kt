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
package com.infomaniak.calendar.components.planning

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import com.infomaniak.calendar.components.planning.component.DayIndicator

/**
 * Lets independent lazy rows of the same day section agree on a minimum total height for the section, so its [DayIndicator]
 * (drawn with zero layout height, see [measureIndicator]) always has room to fit.
 */
@Stable
internal class SectionSizing {
    private val rowHeights = mutableStateMapOf<Any, Int>()
    private var indicatorHeight by mutableIntStateOf(0)

    fun reportIndicatorHeight(height: Int) {
        indicatorHeight = height
    }

    fun reportRowHeight(key: Any, height: Int) {
        if (rowHeights[key] != height) rowHeights[key] = height
    }

    /** Extra height needed so the section is at least as tall as the indicator. */
    fun deficitFor(sectionItemKeys: List<Any>): Int {
        val sectionHeight = sectionItemKeys.sumOf { rowHeights[it] ?: 0 }
        return (indicatorHeight - sectionHeight).coerceAtLeast(0)
    }
}

/**
 * Needed for cases where a section's content is smaller than a [DayIndicator]. It makes the whole section at least the size of a [DayIndicator].
 *
 * Reports this row's height to [sectionSizing], and on the section's last row appends the height deficit as trailing space so the
 * section can fully contain its [DayIndicator].
 */
internal fun Modifier.sectionMinHeight(
    sectionSizing: SectionSizing,
    sectionItemKeys: List<Any>,
    itemKey: Any,
): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    sectionSizing.reportRowHeight(itemKey, placeable.height)

    val isLastInSection = sectionItemKeys.lastOrNull() == itemKey
    val extra = if (isLastInSection) sectionSizing.deficitFor(sectionItemKeys) else 0

    layout(placeable.width, placeable.height + extra) { placeable.place(0, 0) }
}

/**
 * Lays out a [DayIndicator] at zero height, keeping it out of its row's height, while reporting its real height to [sectionSizing]
 * so [sectionMinHeight] can reserve room for it. The indicator still draws at full size (see [stickyDayIndicator]).
 */
internal fun Modifier.measureIndicator(sectionSizing: SectionSizing): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    sectionSizing.reportIndicatorHeight(placeable.height)
    layout(placeable.width, 0) { placeable.place(0, 0) }
}
