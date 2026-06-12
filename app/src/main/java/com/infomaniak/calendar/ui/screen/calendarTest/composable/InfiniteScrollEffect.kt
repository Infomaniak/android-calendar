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
package com.infomaniak.calendar.ui.screen.calendarTest.composable

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Notifies [onReachStart] / [onReachEnd] when the list is scrolled within [buffer] items of either
 * edge. Encapsulates the scroll-position detection so screens stay dumb (they only forward the
 * callbacks, the actual paging decision lives in the ViewModel).
 *
 * Note: [buffer] must stay smaller than the number of items a single page adds, otherwise paging
 * over content-less ranges would stall.
 */
@Composable
internal fun InfiniteScrollEffect(
    listState: LazyListState,
    buffer: Int,
    onReachStart: () -> Unit,
    onReachEnd: () -> Unit,
) {
    val reachedStart by remember(listState) {
        derivedStateOf { listState.firstVisibleItemIndex <= buffer }
    }
    val reachedEnd by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            layoutInfo.totalItemsCount > 0 && lastVisible >= layoutInfo.totalItemsCount - 1 - buffer
        }
    }

    LaunchedEffect(reachedStart) { if (reachedStart) onReachStart() }
    LaunchedEffect(reachedEnd) { if (reachedEnd) onReachEnd() }
}

