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
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest

private const val LOOP_ITEM_COUNT = 100_000
private const val LOOP_START = LOOP_ITEM_COUNT / 2

@Composable
fun rememberScrollPickerState(
    initialIndex: Int = 0,
    group: ScrollPickerGroupState = rememberScrollPickerGroupState(),
): ScrollPickerState {
    return rememberSaveable(group, saver = ScrollPickerState.Saver(group)) { ScrollPickerState(initialIndex, group) }
}

/** @param group links this picker to others sharing it, see [ScrollPickerGroupState]. Defaults to a group of its own. */
@Stable
class ScrollPickerState(initialIndex: Int = 0, val group: ScrollPickerGroupState = ScrollPickerGroupState()) {

    /** Whether this picker is being edited by hand. */
    internal val isEditing: Boolean get() = group.editingPicker == this

    /** Index in the picker's items of the value currently selected. */
    var selectedIndex by mutableIntStateOf(initialIndex)
        internal set

    internal val lazyListState = LazyListState(initialIndex)

    /** Index in [lazyListState] of the selected row, which differs from [selectedIndex] when the picker loops. */
    internal var selectedListIndex = initialIndex

    private var itemCount = 0

    /** Number of items when the picker loops, 0 otherwise. */
    private var loopSize = 0

    val isScrollInProgress: Boolean get() = lazyListState.isScrollInProgress

    /**
     * Centers the selection among [itemCount] items, in the middle of the virtual rows when looping, before the wheel is
     * first measured. Returns the wheel's number of rows.
     */
    internal fun setItems(itemCount: Int, loop: Boolean): Int {
        val looping = loop && itemCount > 0
        val index = selectedIndex.coerceIn(0, (itemCount - 1).coerceAtLeast(0))
        this.itemCount = itemCount
        loopSize = if (looping) itemCount else 0
        selectedListIndex = if (looping) LOOP_START + index else index
        selectedIndex = index
        lazyListState.requestScrollToItem(selectedListIndex)
        return if (looping) LOOP_ITEM_COUNT else itemCount
    }

    /** Index in the picker's items of the one shown by the row at [listIndex]. */
    internal fun itemIndexOf(listIndex: Int) = if (loopSize > 0) (listIndex - LOOP_START).mod(loopSize) else listIndex

    /** Keeps the selection on the row nearest to the wheel's center while it scrolls. */
    internal suspend fun followCenteredRow(itemHeightPx: Int) {
        if (itemCount == 0) return
        snapshotFlow {
            lazyListState.firstVisibleItemIndex + if (lazyListState.firstVisibleItemScrollOffset > itemHeightPx / 2) 1 else 0
        }.collect { listIndex ->
            selectedListIndex = listIndex
            if (!isFollowingSelection) selectedIndex = itemIndexOf(listIndex).coerceIn(0, itemCount - 1)
        }
    }

    /** Whether the wheel is animating towards [selectedIndex], so the rows it passes aren't selected. */
    private var isFollowingSelection = false

    /** Animates the wheel to [selectedIndex] whenever it's set while the wheel shows another row. A user scroll wins. */
    internal suspend fun animateToSelection() {
        snapshotFlow { selectedIndex }.collectLatest { index ->
            if (itemCount > 0 && itemIndexOf(selectedListIndex) != index) {
                isFollowingSelection = true
                try {
                    lazyListState.animateScrollToItem(listIndexOf(index))
                } catch (e: CancellationException) {
                    currentCoroutineContext().ensureActive()
                    // Interrupted by a drag, so keeps what the wheel shows.
                    selectedIndex = itemIndexOf(selectedListIndex).coerceIn(0, itemCount - 1)
                }
            }
            isFollowingSelection = false
        }
    }

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
        var delta = index - itemIndexOf(selectedListIndex)
        if (loopSize > 0) delta = (delta + loopSize / 2).mod(loopSize) - loopSize / 2
        return selectedListIndex + delta
    }

    companion object {
        fun Saver(group: ScrollPickerGroupState): Saver<ScrollPickerState, Int> = Saver<ScrollPickerState, Int>(
            save = { it.selectedIndex },
            restore = { ScrollPickerState(it, group) },
        )
    }
}
