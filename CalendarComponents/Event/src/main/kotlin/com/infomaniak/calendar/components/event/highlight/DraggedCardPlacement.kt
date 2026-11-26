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

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.LayoutDirection

/**
 * Where a card is drawn once it comes loose and follows the finger. The card may only be a piece of its event,
 * like one of its days, while the dragged card can stand for the whole event, its start under the finger.
 */
@Stable
fun interface DraggedCardPlacement {

    /** The dragged card's bounds, relative to the finger, for a [card] of this size grabbed [grab] from its top left. */
    fun bounds(card: Size, grab: Offset, layoutDirection: LayoutDirection): Rect

    companion object {
        /** The card keeps its size and the spot the finger grabbed it at. */
        val AsGrabbed = DraggedCardPlacement { card, grab, _ -> Rect(-grab, card) }
    }
}
