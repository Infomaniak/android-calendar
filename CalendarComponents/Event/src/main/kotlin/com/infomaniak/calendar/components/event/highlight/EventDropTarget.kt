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

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Marks this as where [state]'s held event can be dropped on [date], the drop target nearest to the finger
 * being the one it lands on.
 *
 * [timeAt] tells the time a card dropped here starts at, from where its top and its height are, both as
 * fractions of this target's height: a day's timeline tells it, a month's day doesn't.
 */
fun Modifier.eventDropTarget(
    state: EventHighlightState,
    date: LocalDate,
    timeAt: ((topFraction: Float, heightFraction: Float) -> LocalTime)? = null,
): Modifier = this then EventDropTargetElement(state, date, timeAt)

private data class EventDropTargetElement(
    val state: EventHighlightState,
    val date: LocalDate,
    val timeAt: ((Float, Float) -> LocalTime)?,
) : ModifierNodeElement<EventDropTargetNode>() {
    override fun create() = EventDropTargetNode(state, date, timeAt)
    override fun update(node: EventDropTargetNode) = node.update(state, date, timeAt)
}

internal class EventDropTargetNode(
    private var state: EventHighlightState,
    var date: LocalDate,
    private var timeAt: ((Float, Float) -> LocalTime)?,
) : Modifier.Node(), LayoutAwareModifierNode {

    private var coordinates: LayoutCoordinates? = null

    fun update(state: EventHighlightState, date: LocalDate, timeAt: ((Float, Float) -> LocalTime)?) {
        this.date = date
        this.timeAt = timeAt
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

    /** How far [finger], as seen by [overlay], is from the visible part of this target, or null when none of it shows. */
    fun distanceTo(overlay: LayoutCoordinates, finger: Offset): Float? {
        val target = coordinates?.takeIf { it.isAttached } ?: return null
        val bounds = overlay.localBoundingBoxOf(target, clipBounds = true).takeUnless(Rect::isEmpty) ?: return null
        val nearest = Offset(finger.x.coerceIn(bounds.left, bounds.right), finger.y.coerceIn(bounds.top, bounds.bottom))
        return (finger - nearest).getDistance()
    }

    /** The time a card [cardHeight] high, its top at [cardTop] as seen by [overlay], starts at once dropped here. */
    fun timeAt(overlay: LayoutCoordinates, cardTop: Float, cardHeight: Float): LocalTime? {
        val timeAt = timeAt ?: return null
        val target = coordinates?.takeIf { it.isAttached && it.size.height > 0 } ?: return null
        val height = target.size.height.toFloat()
        val top = target.localPositionOf(overlay, Offset(0f, cardTop)).y
        return timeAt(top / height, cardHeight / height)
    }
}
