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
package com.infomaniak.calendar.ui.screen.calendarTest.paging

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow

/**
 * Bridges the [listState] scroll position to [onScroll] whenever it changes. Pure plumbing: it holds
 * no paging logic (thresholds/decisions live in the ViewModel), it only reports raw positions.
 */
@Composable
internal fun ScrollPositionEffect(
    listState: LazyListState,
    onScroll: (ScrollInfo) -> Unit,
) {
    LaunchedEffect(listState) {
        snapshotFlow {
            ScrollInfo(
                firstVisibleIndex = listState.firstVisibleItemIndex,
                lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0,
                totalItemsCount = listState.layoutInfo.totalItemsCount,
            )
        }.collect(onScroll)
    }
}

