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
package com.infomaniak.calendar.components.eventcard

import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DynamicOverflowRow(
    overflowIndicator: @Composable (overflowCount: Int) -> Unit,
    modifier: Modifier = Modifier.Companion,
    spacing: Dp = 0.dp,
    maxItems: Int = Int.MAX_VALUE,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val spacingPx = spacing.roundToPx()

        // Measure all items with their natural size
        val mainPlaceables = subcompose(Slots.Main, content)
            .map { measurable ->
                measurable.measure(Constraints(maxWidth = Int.MAX_VALUE, maxHeight = Int.MAX_VALUE))
            }

        var totalWidth = 0
        val visibleItems = mutableListOf<Placeable>()

        // Try to fit items greedily
        for (placeable in mainPlaceables) {
            val spacingBefore = if (visibleItems.isEmpty()) 0 else spacingPx
            val newWidth = totalWidth + spacingBefore + placeable.width

            if (visibleItems.size < maxItems && newWidth <= constraints.maxWidth) {
                totalWidth = newWidth
                visibleItems.add(placeable)
            } else {
                break
            }
        }

        var overflowPlaceable: Placeable? = null
        val overflowCount = mainPlaceables.size - visibleItems.size

        if (overflowCount > 0) {
            overflowPlaceable = subcompose(Slots.Overflow) {
                overflowIndicator(overflowCount)
            }.first().measure(Constraints(maxWidth = Int.MAX_VALUE, maxHeight = Int.MAX_VALUE))

            // Remove items to make room for overflow indicator
            while (
                visibleItems.isNotEmpty() &&
                totalWidth + spacingPx + overflowPlaceable.width > constraints.maxWidth
            ) {
                val removed = visibleItems.removeAt(visibleItems.lastIndex)
                totalWidth -= removed.width
                if (visibleItems.isNotEmpty()) {
                    totalWidth -= spacingPx
                }
            }

            totalWidth += (if (visibleItems.isNotEmpty()) spacingPx else 0) + overflowPlaceable.width
        }

        val layoutWidth = constraints.maxWidth
        val layoutHeight = maxOf(
            visibleItems.maxOfOrNull { it.height } ?: 0,
            overflowPlaceable?.height ?: 0,
        )

        layout(layoutWidth, layoutHeight) {
            // Calculate total content width
            var contentWidth = 0
            visibleItems.forEach {
                contentWidth += it.width
            }
            if (visibleItems.isNotEmpty() && overflowPlaceable != null) {
                contentWidth += spacingPx
            }
            if (overflowPlaceable != null) {
                contentWidth += overflowPlaceable.width
            }

            // Add spacing between items
            if (visibleItems.size > 1) {
                contentWidth += (visibleItems.size - 1) * spacingPx
            }
            if (visibleItems.isNotEmpty() && overflowPlaceable != null) {
                contentWidth += spacingPx
            }

            // Calculate start position based on alignment
            val startX = horizontalAlignment.align(contentWidth, layoutWidth, layoutDirection)
            var x = startX

            visibleItems.forEach { placeable ->
                placeable.placeRelative(x, 0)
                x += placeable.width + spacingPx
            }

            overflowPlaceable?.placeRelative(x, 0)
        }
    }
}

private enum class Slots {
    Main, Overflow
}
