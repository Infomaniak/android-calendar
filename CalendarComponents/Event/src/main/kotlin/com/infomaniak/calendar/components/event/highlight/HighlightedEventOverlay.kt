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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.event.EventItem
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.models.SimpleEventSummaryUi
import com.infomaniak.calendar.components.foundation.preview.LocalEventColorsUiFactory
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Instant

private val HighlightElevation = 8.dp
private val FingerClearance = 24.dp

/**
 * Draws the event [state] holds up above everything it covers, the neighbouring days included.
 *
 * The card unfolds from its place in its column to the width of its area, a margin off each side, written the
 * way the Planning writes an event. It opens just above the finger so the finger doesn't hide it, or just
 * below when there is no room above, and always fully inside the area, out of the [contentPadding]
 * where bars may be drawn over the overlay. Pulled or dragged, it is shifted after the finger, and once
 * dragged, it is drawn by the highlight's own dragged card, where its [DraggedCardPlacement] puts it, until it is
 * back where it sat.
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
                val cardModifier = Modifier.graphicsLayer {
                    val elevation = when {
                        state.isDragging -> 1f
                        state.returningFrom != null -> 1f - state.returnProgress.coerceIn(0f, 1f)
                        else -> state.progress.coerceIn(0f, 1f)
                    }
                    shadowElevation = HighlightElevation.toPx() * elevation
                    shape = cardShape
                }.background(backingColor, cardShape)

                val draggedCard = highlight.draggedCard
                if ((state.isDragging || state.returningFrom != null) && draggedCard != null) {
                    draggedCard(cardModifier)
                } else {
                    EventItem(event = highlight.event, onClick = null, modifier = cardModifier)
                }
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
            val height = measurable.minIntrinsicHeight((area.width - 2 * margin).roundToInt().coerceAtLeast(0)).toFloat()
                .coerceAtMost(area.height - 2 * margin)
                .coerceAtLeast(0f)
            val unfolded = unfoldedBounds(highlight.fingerY, area, height, margin, clearance)
            val returningFrom = state.returningFrom
            val bounds = if (returningFrom != null) {
                lerp(returningFrom, highlight.startBounds, state.returnProgress)
            } else {
                val home = if (state.isDragging) highlight.draggedBounds else highlight.startBounds
                lerp(home, unfolded, state.progress).translate(state.dragOffset)
            }

            measurable.measure(
                Constraints.fixed(bounds.width.roundToInt().coerceAtLeast(0), bounds.height.roundToInt().coerceAtLeast(0)),
            ) to bounds
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            card?.let { (placeable, bounds) -> placeable.place(bounds.left.roundToInt(), bounds.top.roundToInt()) }
        }
    }
}

/** Across the [area], [clearance] above [fingerY] (below it if it doesn't fit), kept [margin] inside the area on every side. */
private fun unfoldedBounds(fingerY: Float, area: Rect, height: Float, margin: Float, clearance: Float): Rect {
    val above = fingerY - clearance - height
    val top = (if (above >= area.top + margin) above else fingerY + clearance)
        .coerceAtMost(area.bottom - margin - height)
        .coerceAtLeast(area.top + margin)

    return Rect(left = area.left + margin, top = top, right = area.right - margin, bottom = top + height)
}

@Preview
@Composable
private fun UnfoldingPreview() {
    HighlightedEventOverlayPreview(progress = 0.5f)
}

@Preview
@Composable
private fun UnfoldedPreview() {
    HighlightedEventOverlayPreview(progress = 1f)
}

@Composable
private fun HighlightedEventOverlayPreview(progress: Float) {
    val density = LocalDensity.current
    val event = SimpleEventSummaryUi(
        id = "preview",
        title = "Weekly sync with the whole team",
        location = "Meeting room 3",
        status = EventStatus.Confirmed,
        start = Instant.parse("2025-01-01T10:00:00Z"),
        end = Instant.parse("2025-01-01T11:00:00Z"),
        isAllDay = false,
        colors = LocalEventColorsUiFactory.current.create(0xFF0098FF.toInt()),
        hasAttendees = true,
        myStatus = ParticipationStatus.Accepted,
    )
    // A compressed card in the middle one of three day columns, next to a 48dp hour gutter.
    val highlight = with(density) {
        EventHighlight(
            event = event,
            key = event,
            startBounds = Rect(left = 152.dp.toPx(), top = 200.dp.toPx(), right = 248.dp.toPx(), bottom = 260.dp.toPx()),
            areaBounds = Rect(left = 48.dp.toPx(), top = 0f, right = 360.dp.toPx(), bottom = 480.dp.toPx()),
            fingerY = 230.dp.toPx(),
        )
    }
    val scope = rememberCoroutineScope()
    val state = remember(scope) { EventHighlightState(scope, highlight, progress) }

    MaterialTheme {
        Surface(Modifier.size(width = 360.dp, height = 480.dp)) {
            HighlightedEventOverlay(state, Modifier.fillMaxSize())
        }
    }
}
