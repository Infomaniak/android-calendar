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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.TraversableNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Marks the area a highlighted event unfolds across: its whole width, and the height it can show, inside
 * [padding]. A card held by [highlightOnLongPress] unfolds across the nearest of these around it.
 */
fun Modifier.eventHighlightArea(padding: PaddingValues = PaddingValues()): Modifier {
    return this then EventHighlightAreaElement(padding)
}

private data class EventHighlightAreaElement(val padding: PaddingValues) : ModifierNodeElement<EventHighlightAreaNode>() {
    override fun create() = EventHighlightAreaNode(padding)
    override fun update(node: EventHighlightAreaNode) {
        node.padding = padding
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "eventHighlightArea"
        properties["padding"] = padding
    }
}

internal class EventHighlightAreaNode(var padding: PaddingValues) : Modifier.Node(), TraversableNode {
    override val traverseKey: Any = TraverseKey

    /** The [bounds] of the area, without its [padding]. */
    fun inset(bounds: Rect, density: Density, layoutDirection: LayoutDirection): Rect = with(density) {
        Rect(
            left = bounds.left + padding.calculateLeftPadding(layoutDirection).toPx(),
            top = bounds.top + padding.calculateTopPadding().toPx(),
            right = bounds.right - padding.calculateRightPadding(layoutDirection).toPx(),
            bottom = bounds.bottom - padding.calculateBottomPadding().toPx(),
        )
    }

    companion object TraverseKey
}
