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
package com.infomaniak.scrollpicker

import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.lazy.LazyListState
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sign

/** Snaps on the item nearest to where the fling decays, picked as the fling starts so haptics know where it settles. */
internal class ScrollPickerSnapper(
    private val listState: LazyListState,
    private val itemHeightPx: Int,
) : SnapLayoutInfoProvider {

    /** The row the current fling settles on, -1 when not flinging. */
    var target = -1L

    /** Scroll offset of the wheel, in pixels. */
    val position get() = listState.firstVisibleItemIndex.toLong() * itemHeightPx + listState.firstVisibleItemScrollOffset

    override fun calculateApproachOffset(velocity: Float, decayOffset: Float): Float {
        val lastItem = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0).toLong()
        val nearest = (position + decayOffset.roundToLong() + itemHeightPx / 2).floorDiv(itemHeightPx)
        target = nearest.coerceIn(0, lastItem)
        return (abs(decayOffset) - itemHeightPx).coerceAtLeast(0f) * sign(decayOffset)
    }

    override fun calculateSnapOffset(velocity: Float): Float = (target * itemHeightPx - position).toFloat()
}
