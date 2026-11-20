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

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Lets the event [state] holds go as soon as a second finger lands anywhere in this area, for a pinch.
 *
 * Placed around everything the fingers can touch, it sees a second finger the held card never gets
 * (one on another day or on an empty hour), ahead of the card and the timelines it encloses.
 */
internal fun Modifier.releaseHighlightOnSecondPointer(state: EventHighlightState): Modifier = pointerInput(state) {
    awaitEachGesture {
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            if (event.changes.count { it.pressed } > 1) state.hide()
        } while (event.changes.any { it.pressed })
    }
}
