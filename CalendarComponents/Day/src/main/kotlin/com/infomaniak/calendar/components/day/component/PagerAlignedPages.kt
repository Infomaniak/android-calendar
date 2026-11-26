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
package com.infomaniak.calendar.components.day.component

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout

/**
 * The [pageContent] of each page [pagerState] shows, placed where the pager places that page.
 *
 * Not a pager of its own: two pagers could only be kept in step after the fact, a frame late. Here
 * the pager's own layout positions the pages, so whatever sits above the days follows them exactly.
 */
@Composable
internal fun PagerAlignedPages(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    pageContent: @Composable (page: Int) -> Unit,
) {
    val visiblePages by remember(pagerState) {
        derivedStateOf { pagerState.layoutInfo.visiblePagesInfo.map { it.index } }
    }

    Layout(
        modifier = modifier.clipToBounds(),
        content = {
            visiblePages.forEach { page ->
                key(page) { pageContent(page) }
            }
        },
    ) { measurables, constraints ->
        val layoutInfo = pagerState.layoutInfo
        val pageWidth = layoutInfo.pageSize.coerceAtLeast(0)
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = pageWidth, maxWidth = pageWidth)) }
        val height = placeables.maxOfOrNull { it.height } ?: constraints.minHeight

        layout(constraints.maxWidth, height) {
            placeables.forEachIndexed { index, placeable ->
                val pageInfo = layoutInfo.visiblePagesInfo.firstOrNull { it.index == visiblePages.getOrNull(index) }
                pageInfo?.let { placeable.placeRelative(x = it.offset, y = 0) }
            }
        }
    }
}
