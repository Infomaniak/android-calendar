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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Keeps a day indicator at the top of the list while its day's item scrolls under it, never leaving that item. */
internal fun Modifier.stickyDayIndicator(lazyListState: LazyListState, index: Int): Modifier = graphicsLayer {
    val layoutInfo = lazyListState.layoutInfo
    val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return@graphicsLayer
    val contentOrigin = layoutInfo.viewportStartOffset + layoutInfo.beforeContentPadding
    val maxOffset = (itemInfo.size - size.height).coerceAtLeast(0f)
    translationY = (contentOrigin - itemInfo.offset).toFloat().coerceIn(0f, maxOffset)
}
