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
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.TraversableNode

/**
 * Marks the area a highlighted event unfolds across: its whole width, and the height it can show.
 * A card held by [highlightOnLongPress] unfolds across the nearest of these around it.
 */
internal fun Modifier.eventHighlightArea(): Modifier = this then EventHighlightAreaElement

private data object EventHighlightAreaElement : ModifierNodeElement<EventHighlightAreaNode>() {
    override fun create() = EventHighlightAreaNode()
    override fun update(node: EventHighlightAreaNode) = Unit
}

internal class EventHighlightAreaNode : Modifier.Node(), TraversableNode {
    override val traverseKey: Any = TraverseKey

    companion object TraverseKey
}
