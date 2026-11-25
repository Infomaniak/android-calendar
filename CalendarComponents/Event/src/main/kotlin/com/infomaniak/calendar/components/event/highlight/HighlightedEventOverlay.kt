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
package com.infomaniak.calendar.components.event.highlight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.event.EventItem
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val HighlightElevation = 8.dp
private val FingerClearance = 24.dp

/**
 * Draws the event [state] holds up above everything it covers, the neighbouring days included.
 *
 * The card unfolds from its place in its column to the whole width of its area, written the way the
 * Planning writes an event. It opens just above the finger so the finger doesn't hide it, or just
 * below when there is no room above, and always fully inside the area, out of the [contentPadding]
 * where bars may be drawn over the overlay.
 */
@Composable
fun HighlightedEventOverlay(
    state: EventHighlightState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val highlight = state.highlight
    val cardShape = MaterialTheme.shapes.small
    // The card colours are translucent tints, so an opaque backing keeps the events below from showing through.
    val backingColor = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current
    val margin = with(density) { EsdsTheme.spacing.md.toPx() }
    val clearance = with(density) { FingerClearance.toPx() }

    Layout(
        modifier = modifier
            .letPointersThrough()
            .onPlaced { state.overlayCoordinates = it },
        content = {
            if (highlight != null) {
                EventItem(
                    event = highlight.event,
                    onClick = null,
                    modifier = Modifier.graphicsLayer {
                        shadowElevation = HighlightElevation.toPx() * state.progress.coerceIn(0f, 1f)
                        shape = cardShape
                    }.background(backingColor, cardShape),
                )
            }
        },
    ) { measurables, constraints ->
        val card = highlight?.let { measurables.firstOrNull() }?.let { measurable ->
            val area = highlight.areaBounds.let {
                it.copy(
                    top = max(it.top, contentPadding.calculateTopPadding().toPx()),
                    bottom = min(it.bottom, constraints.maxHeight - contentPadding.calculateBottomPadding().toPx()),
                )
            }
            // A short area (landscape, large font) can't fit the whole card: it is clipped rather than pushed out of the area.
            val height = measurable.minIntrinsicHeight(area.width.roundToInt()).toFloat()
                .coerceAtMost(area.height - 2 * margin)
                .coerceAtLeast(0f)
            val unfolded = unfoldedBounds(highlight.fingerY, area, height, margin, clearance)
            val bounds = lerp(highlight.startBounds, unfolded, state.progress)

            measurable.measure(
                Constraints.fixed(bounds.width.roundToInt().coerceAtLeast(0), bounds.height.roundToInt().coerceAtLeast(0)),
            ) to bounds
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            card?.let { (placeable, bounds) -> placeable.place(bounds.left.roundToInt(), bounds.top.roundToInt()) }
        }
    }
}

/** Across the whole [area], [clearance] above [fingerY] (below it if it doesn't fit), kept [margin] inside the area. */
private fun unfoldedBounds(fingerY: Float, area: Rect, height: Float, margin: Float, clearance: Float): Rect {
    val above = fingerY - clearance - height
    val top = (if (above >= area.top + margin) above else fingerY + clearance)
        .coerceAtMost(area.bottom - margin - height)
        .coerceAtLeast(area.top + margin)

    return Rect(left = area.left, top = top, right = area.right, bottom = top + height)
}
