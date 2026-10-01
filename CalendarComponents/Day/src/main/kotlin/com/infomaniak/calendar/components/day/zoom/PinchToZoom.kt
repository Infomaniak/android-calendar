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
package com.infomaniak.calendar.components.day.zoom

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.pow

/**
 * Zooms the given axes around the fingers of a pinch. Either axis can be left out: it is then
 * neither zoomed nor given a share of the pinch, which the other axis gets in full.
 *
 * The pinch is handed to each axis in the coordinates of the element this modifier is set on, so
 * where it sits is up to the axes it zooms: see [ZoomableAxis].
 *
 * It listens ahead of the content it covers, so that a scrollable laid inside it, a lazy list
 * especially, does not drag along with the fingers of a pinch.
 */
internal fun Modifier.pinchToZoom(
    horizontal: ZoomableAxis? = null,
    vertical: ZoomableAxis? = null,
): Modifier = pointerInput(horizontal, vertical) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        var isPinching = false
        var anchors: PinchAnchors? = null

        try {
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)

                if (event.changes.count { it.pressed } > 1) {
                    isPinching = true
                    horizontal?.isZooming = true
                    vertical?.isZooming = true

                    val centroid = event.calculateCentroid()
                    if (centroid.isSpecified) {
                        val pinchAnchors = anchors ?: PinchAnchors(
                            horizontal = horizontal?.anchorAt(pointerOffset = centroid.x),
                            vertical = vertical?.anchorAt(pointerOffset = centroid.y),
                        )

                        val zoom = event.calculateZoom()
                        val horizontalShare = when {
                            vertical == null -> 1f
                            horizontal == null -> 0f
                            else -> event.horizontalShareAround(centroid)
                        }

                        pinchAnchors.horizontal?.let { horizontal?.zoomAround(it, factor = zoom.pow(horizontalShare)) }
                        pinchAnchors.vertical?.let { vertical?.zoomAround(it, factor = zoom.pow(1f - horizontalShare)) }
                        anchors = pinchAnchors
                    }
                } else {
                    anchors = null
                }

                if (isPinching) event.changes.forEach { it.consume() }
            } while (event.changes.any { it.pressed })
        } finally {
            horizontal?.isZooming = false
            vertical?.isZooming = false
        }
    }
}

private class PinchAnchors(val horizontal: ZoomAnchor?, val vertical: ZoomAnchor?)

/**
 * How much of a pinch goes to the horizontal axis: none for fingers lined up vertically, all of it
 * for fingers lined up horizontally, and a part in between for a diagonal pinch, the vertical axis
 * getting the rest.
 *
 * Each axis zooms by the pinch's zoom raised to its share, so the two zooms always multiply back to
 * the zoom of the pinch, and the split follows the angle of the fingers smoothly rather than
 * snapping from one axis to the other.
 */
private fun PointerEvent.horizontalShareAround(centroid: Offset): Float {
    var horizontalSpread = 0f
    var verticalSpread = 0f

    changes.forEach { change ->
        if (change.pressed) {
            val distance = change.position - centroid
            horizontalSpread += distance.x * distance.x
            verticalSpread += distance.y * distance.y
        }
    }

    val totalSpread = horizontalSpread + verticalSpread
    return if (totalSpread == 0f) 0.5f else horizontalSpread / totalSpread
}
