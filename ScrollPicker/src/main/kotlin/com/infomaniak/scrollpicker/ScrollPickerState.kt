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
fun rememberScrollPickerState(initialIndex: Int = 0): ScrollPickerState {
    return rememberSaveable(saver = ScrollPickerState.Saver) { ScrollPickerState(initialIndex) }
}

@Stable
class ScrollPickerState(initialIndex: Int = 0) {

    /** Index in the picker's items of the value currently selected. */
    var selectedIndex by mutableIntStateOf(initialIndex)
        internal set

    // TODO: Map to/from selectedIndex, accounting for the offset of the loop's virtual items.
    internal val lazyListState = LazyListState()

    val isScrollInProgress: Boolean get() = lazyListState.isScrollInProgress

    suspend fun scrollToIndex(index: Int) {
        // TODO
    }

    suspend fun animateScrollToIndex(index: Int) {
        // TODO
    }

    companion object {
        val Saver: Saver<ScrollPickerState, Int> = Saver(save = { it.selectedIndex }, restore = ::ScrollPickerState)
    }
}
