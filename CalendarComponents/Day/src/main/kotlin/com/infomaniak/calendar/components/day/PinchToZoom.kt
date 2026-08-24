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
package com.infomaniak.calendar.components.day

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.PinchAnchor

internal fun Modifier.pinchToZoom(state: DayTimelineState): Modifier = pointerInput(state) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var anchor: PinchAnchor? = null

        try {
            do {
                val event = awaitPointerEvent()
                val pressed = event.changes.count { it.pressed }

                if (pressed > 1) {
                    state.isPinching = true

                    val centroid = event.calculateCentroid()
                    if (centroid.isSpecified) {
                        val pinchAnchor = anchor ?: state.anchorAt(contentY = centroid.y, density = this)

                        state.zoomAround(pinchAnchor, zoom = event.calculateZoom(), density = this)
                        anchor = pinchAnchor
                    }
                } else {
                    anchor = null
                }

                if (state.isPinching) event.changes.forEach { it.consume() }
            } while (event.changes.any { it.pressed })
        } finally {
            state.isPinching = false
        }
    }
}
