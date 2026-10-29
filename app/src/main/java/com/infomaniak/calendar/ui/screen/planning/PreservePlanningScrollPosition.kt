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
package com.infomaniak.calendar.ui.screen.planning

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.infomaniak.calendar.components.planning.PlanningItemKey
import kotlin.math.abs

/**
 * Resolve the viewport's key in the whole new page window, not just LazyColumn's nearby key cache.
 * Do this before the next measure so a large prepend never displays the old index in the new data.
 */
@Composable
internal fun PreservePlanningScrollPosition(
    keys: List<PlanningItemKey>,
    lazyListState: LazyListState,
    enabled: Boolean,
) {
    val window = remember { PlanningScrollWindow() }

    SideEffect {
        val previousKeys = window.keys
        window.keys = keys
        if (previousKeys === keys || !enabled || lazyListState.isScrollInProgress) return@SideEffect

        val visibleItem = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == lazyListState.firstVisibleItemIndex }
        val key = visibleItem?.key as? PlanningItemKey ?: return@SideEffect
        val anchor = PlanningScrollAnchor(key, lazyListState.firstVisibleItemScrollOffset)
        val position = anchor.resolve(keys, previousKeys) ?: return@SideEffect
        if (position.index != lazyListState.firstVisibleItemIndex || position.scrollOffset != anchor.scrollOffset) {
            lazyListState.requestScrollToItem(position.index, position.scrollOffset)
        }
    }
}

private class PlanningScrollWindow {
    var keys: List<PlanningItemKey> = emptyList()
}

internal data class PlanningScrollPosition(val index: Int, val scrollOffset: Int)

internal data class PlanningScrollAnchor(val key: PlanningItemKey, val scrollOffset: Int) {
    fun resolve(keys: List<PlanningItemKey>, previousKeys: List<PlanningItemKey>): PlanningScrollPosition? {
        val index = keys.indexOf(key)
        if (index >= 0) return PlanningScrollPosition(index, scrollOffset)

        // The event may have been deleted, or an empty-day row replaced by actual events.
        val indices = keys.withIndex().associate { it.value to it.index }
        val previousIndex = previousKeys.indexOf(key)
        val neighbor = previousKeys.withIndex()
            .filter { it.value.date == key.date && it.value in indices }
            .minByOrNull { abs(it.index - previousIndex) }
        val fallbackIndex = neighbor?.let { indices[it.value] }
            ?: keys.indexOfFirst { it.date >= key.date }.takeIf { it >= 0 }
            ?: keys.lastIndex.takeIf { it >= 0 }
        return fallbackIndex?.let { PlanningScrollPosition(it, 0) }
    }
}
