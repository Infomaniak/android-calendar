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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

@Composable
fun rememberEventCardState(): EventCardState {
    return remember { EventCardState() }
}

@Stable
class EventCardState {
    var collapsedHeight by mutableStateOf<Int?>(null)
    var expandedHeight by mutableStateOf<Int?>(null)

    fun recordHeights(collapsedHeight: Int, expandedHeight: Int) {
        this.collapsedHeight = collapsedHeight
        this.expandedHeight = expandedHeight
    }

    fun computeProgress(currentCardSize: Dp?, density: Density): Float {
        val currentCardHeight = with(density) { currentCardSize?.toPx() } ?: return 1f
        val collapsedHeight = collapsedHeight ?: return 1f
        val expandedHeight = expandedHeight ?: return 1f
        return (currentCardHeight - collapsedHeight) / (expandedHeight - collapsedHeight)
    }
}
