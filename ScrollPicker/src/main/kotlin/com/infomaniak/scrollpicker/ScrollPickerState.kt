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

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun rememberScrollPickerState(initialIndex: Int = 0, group: ScrollPickerGroupState? = null): ScrollPickerState {
    return rememberSaveable(group, saver = ScrollPickerState.Saver(group)) { ScrollPickerState(initialIndex, group) }
}

/** @param group links this picker to others sharing it, see [ScrollPickerGroupState]. */
@Stable
class ScrollPickerState(initialIndex: Int = 0, val group: ScrollPickerGroupState? = null) {

    /** Index in the picker's items of the value currently selected. */
    var selectedIndex by mutableIntStateOf(initialIndex)
        internal set

    internal val lazyListState = LazyListState(initialIndex)

    /** Index in [lazyListState] of the selected row, which differs from [selectedIndex] when the picker loops. */
    internal var selectedListIndex = initialIndex

    /** Number of items when the picker loops, 0 otherwise. */
    internal var loopSize = 0

    val isScrollInProgress: Boolean get() = lazyListState.isScrollInProgress

    suspend fun scrollToIndex(index: Int) = lazyListState.scrollToItem(listIndexOf(index))

    suspend fun animateScrollToIndex(index: Int) = lazyListState.animateScrollToItem(listIndexOf(index))

    /** Selects [index] immediately, without waiting for the wheel to scroll there. */
    internal fun select(index: Int) {
        val listIndex = listIndexOf(index)
        lazyListState.requestScrollToItem(listIndex)
        selectedListIndex = listIndex
        selectedIndex = index
    }

    /** The row showing the item at [index], taking the shortest way round when looping. */
    private fun listIndexOf(index: Int): Int {
        var delta = index - selectedIndex
        if (loopSize > 0) delta = (delta + loopSize / 2).mod(loopSize) - loopSize / 2
        return selectedListIndex + delta
    }

    companion object {
        fun Saver(group: ScrollPickerGroupState?): Saver<ScrollPickerState, Int> = Saver<ScrollPickerState, Int>(
            save = { it.selectedIndex },
            restore = { ScrollPickerState(it, group) },
        )
    }
}
