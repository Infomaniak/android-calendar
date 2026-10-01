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

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow

/**
 * [initialZoom] is read once, when the state is built, and is deliberately not a key of the
 * remember: the state owns its zoom from then on, so a pinch in progress survives a recomposition
 * rather than being reset to the zoom the caller first asked for.
 */
@Composable
internal fun rememberZoomableAxisState(
    initialZoom: Float,
    zoomRange: ClosedFloatingPointRange<Float>,
    scrollState: ScrollState,
): ZoomableAxisState {
    val state = remember(scrollState, zoomRange) { ZoomableAxisState(initialZoom, zoomRange, scrollState) }

    // A new scroll range is the content reporting the size a zoom asked it for, which is the first
    // moment the zoom's own scroll can be honoured in full.
    LaunchedEffect(state) {
        snapshotFlow { scrollState.maxValue }.collect { state.reanchorAfterZoom() }
    }

    return state
}

/**
 * Zoom and scroll along one axis of a timeline, kept in step so that zooming around a point leaves
 * that point where it was in the viewport.
 *
 * [zoom] is a factor over the base length of the axis' unit, whatever that unit is (an hour on the
 * vertical axis, a day on the horizontal one): the content along the axis is expected to grow in
 * proportion to it. Knowing nothing more about its axis, the same state serves either of them.
 */
@Stable
internal class ZoomableAxisState(
    initialZoom: Float,
    private val zoomRange: ClosedFloatingPointRange<Float>,
    val scrollState: ScrollState,
) {

    private var clampedZoom by mutableFloatStateOf(initialZoom.coerceIn(zoomRange))

    var zoom: Float
        get() = clampedZoom
        set(value) {
            clampedZoom = value.coerceIn(zoomRange)
        }

    var isZooming: Boolean by mutableStateOf(false)

    /** The anchor of the zoom that is still waiting for the content to be measured at its new size. */
    private var heldAnchor: ZoomAnchor? = null

    /**
     * Pins the point at [contentOffset], expressed in the coordinate system of the whole content
     * rather than of the viewport, so it can be kept under the fingers while zooming.
     */
    fun anchorAt(contentOffset: Float): ZoomAnchor = ZoomAnchor(
        unzoomedOffset = contentOffset / zoom,
        viewportOffset = contentOffset - scrollState.value,
    )

    fun zoomAround(anchor: ZoomAnchor, factor: Float) {
        zoom *= factor
        heldAnchor = anchor

        holdAnchor(anchor)
    }

    /**
     * Puts the anchored point back under the fingers once the content has been measured at its new
     * size.
     *
     * Zooming only asks for that measure, so the scroll range is still the previous, shorter one
     * while the pinch is being handled: near the end of the content a scroll towards it is clipped
     * to that range, and what was clipped off would never be asked for again.
     */
    fun reanchorAfterZoom() {
        val anchor = heldAnchor ?: return
        heldAnchor = null

        holdAnchor(anchor)
    }

    private fun holdAnchor(anchor: ZoomAnchor) {
        val newContentOffset = anchor.unzoomedOffset * zoom
        val newViewportOffset = newContentOffset - scrollState.value

        scrollState.dispatchRawDelta(newViewportOffset - anchor.viewportOffset)
    }
}

/**
 * @param unzoomedOffset Where the anchored point lies in the content at a zoom of 1, which stays
 * the same whatever the zoom, unlike its offset in the zoomed content.
 * @param viewportOffset Where the anchored point lies inside the viewport, which is where it has
 * to stay while zooming.
 */
@Immutable
internal data class ZoomAnchor(val unzoomedOffset: Float, val viewportOffset: Float)
