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
package com.infomaniak.calendar.components.planning

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged

/**
 * Offsets the content vertically so it stays pinned to the top of the [lazyListState] viewport while its
 * parent item is scrolled off the top of the screen, without ever crossing the bottom edge of that item.
 *
 * Behaves like an in-item sticky header: the element follows the scroll up to the point where its bottom
 * reaches the item's bottom, then travels up with the item as it leaves the viewport.
 *
 * @param key the lazy list item key this content belongs to (used to locate the item in the layout info).
 */
@Composable
fun Modifier.stickyWithinItem(lazyListState: LazyListState, key: Any): Modifier {
    var contentHeight by remember { mutableIntStateOf(0) }

    return this
        .onSizeChanged { contentHeight = it.height }
        .graphicsLayer {
            val itemInfo = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }
                ?: return@graphicsLayer

            val itemTop = itemInfo.offset - lazyListState.layoutInfo.viewportStartOffset
            val maxOffset = (itemInfo.size - contentHeight).toFloat().coerceAtLeast(0f)
            translationY = (-itemTop).toFloat().coerceIn(0f, maxOffset)
        }
}
