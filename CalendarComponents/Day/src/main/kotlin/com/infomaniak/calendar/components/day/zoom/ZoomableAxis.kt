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

import androidx.compose.runtime.Immutable

/**
 * One axis a pinch can zoom, keeping the point under the fingers where it was in the viewport.
 *
 * How the axis scrolls is its own business, as is the coordinate system it reads the pinch in: each
 * implementation states where [pinchToZoom] has to sit for the pointer offsets to mean what it
 * expects.
 */
internal interface ZoomableAxis {

    var isZooming: Boolean

    /** Pins the point at [pointerOffset], so it can be kept under the fingers while zooming. */
    fun anchorAt(pointerOffset: Float): ZoomAnchor

    fun zoomAround(anchor: ZoomAnchor, factor: Float)
}

/**
 * The same axis, read from an element [offset] pixels before the one it expects the pinch on: lets a
 * single [pinchToZoom] cover an area wider than what each of its axes scrolls.
 */
internal fun ZoomableAxis.offsetBy(offset: () -> Float): ZoomableAxis = object : ZoomableAxis by this {
    override fun anchorAt(pointerOffset: Float): ZoomAnchor = this@offsetBy.anchorAt(pointerOffset + offset())
}

/**
 * @param position Where the anchored point lies along the axis, in a unit the zoom leaves unchanged,
 * unlike its offset in the zoomed content.
 * @param viewportOffset Where the anchored point lies inside the viewport, which is where it has
 * to stay while zooming.
 */
@Immutable
internal data class ZoomAnchor(val position: Float, val viewportOffset: Float)
