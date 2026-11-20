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
package com.infomaniak.calendar.components.day.highlight

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import com.infomaniak.calendar.components.day.model.MINUTES_PER_DAY
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt

private const val DROP_STEP_MINUTES = 15

/**
 * Marks a day's column, running from midnight to midnight, as somewhere [state]'s event can be
 * dropped on [date].
 */
internal fun Modifier.eventDropTarget(state: EventHighlightState, date: LocalDate): Modifier {
    return this then EventDropTargetElement(state, date)
}

private data class EventDropTargetElement(
    val state: EventHighlightState,
    val date: LocalDate,
) : ModifierNodeElement<EventDropTargetNode>() {
    override fun create() = EventDropTargetNode(state, date)
    override fun update(node: EventDropTargetNode) = node.update(state, date)
}

internal class EventDropTargetNode(
    private var state: EventHighlightState,
    var date: LocalDate,
) : Modifier.Node(), LayoutAwareModifierNode {

    private var coordinates: LayoutCoordinates? = null

    fun update(state: EventHighlightState, date: LocalDate) {
        this.date = date
        if (state == this.state) return
        this.state.dropTargets -= this
        this.state = state
        if (isAttached) state.dropTargets += this
    }

    override fun onAttach() {
        state.dropTargets += this
    }

    override fun onDetach() {
        state.dropTargets -= this
        coordinates = null
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        this.coordinates = coordinates
    }

    /** Whether the column, as seen by [overlay], spans [x]. */
    fun containsX(overlay: LayoutCoordinates, x: Float): Boolean {
        val column = coordinates?.takeIf { it.isAttached } ?: return false
        val bounds = overlay.localBoundingBoxOf(column, clipBounds = false)
        return x >= bounds.left && x < bounds.right
    }

    /** The quarter of an hour nearest to [y], as seen by [overlay], leaving room for [durationMinutes] before midnight. */
    fun minuteOfDayAt(overlay: LayoutCoordinates, y: Float, durationMinutes: Int): Int? {
        val column = coordinates?.takeIf { it.isAttached && it.size.height > 0 } ?: return null
        val localY = column.localPositionOf(overlay, Offset(0f, y)).y
        val minute = localY / column.size.height * MINUTES_PER_DAY
        val snapped = (minute / DROP_STEP_MINUTES).roundToInt() * DROP_STEP_MINUTES
        return snapped.coerceIn(0, MINUTES_PER_DAY - durationMinutes.coerceIn(DROP_STEP_MINUTES, MINUTES_PER_DAY))
    }
}
