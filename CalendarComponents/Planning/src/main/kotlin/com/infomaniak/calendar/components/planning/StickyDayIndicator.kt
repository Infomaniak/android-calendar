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

/**
 * Keeps a day indicator pinned to the viewport top for the lifetime of its section, handing off between items
 * as they scroll, and clamped so it never overlaps the next section.
 *
 * Only the first visible item of the section actually draws the indicator (others use `alpha = 0f`, keeping
 * their layout width so [EventItem]s stay aligned). Expects the indicator to be laid out at zero height (see
 * [measureIndicator]) and the section to reserve enough room for it (see [sectionMinHeight]).
 *
 * Runs entirely in the draw phase via [graphicsLayer], so scrolling triggers no recomposition.
 */
internal fun Modifier.stickyDayIndicator(
    lazyListState: LazyListState,
    itemKey: Any,
    sectionItemKeys: List<Any>,
): Modifier = graphicsLayer {
    val layoutInfo = lazyListState.layoutInfo
    val visibleKeys = layoutInfo.visibleItemsInfo.mapTo(mutableSetOf()) { it.key }

    val ownerKey = sectionItemKeys.firstOrNull { it in visibleKeys }
    if (ownerKey != itemKey) {
        alpha = 0f
        return@graphicsLayer
    }

    val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.key == itemKey } ?: return@graphicsLayer
    val lazyListContentOriginOffset = layoutInfo.viewportStartOffset + layoutInfo.beforeContentPadding
    val itemDistanceFromContentOrigin = itemInfo.offset - lazyListContentOriginOffset

    // Clamp against the section's bottom edge (bottom of its last item) so the indicator never spills into
    // the next section. If the last item isn't visible, the section extends past the viewport: no clamp needed.
    val lastItemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.key == sectionItemKeys.lastOrNull() }
    val maxOffset = if (lastItemInfo != null) {
        val sectionBottom = lastItemInfo.offset + lastItemInfo.size - lazyListContentOriginOffset
        sectionBottom - size.height - itemDistanceFromContentOrigin
    } else {
        Float.POSITIVE_INFINITY
    }
    translationY = (-itemDistanceFromContentOrigin).toFloat().coerceIn(minOf(0f, maxOffset), maxOffset)
}
