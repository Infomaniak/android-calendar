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
package com.infomaniak.calendar.components.foundation.utils

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * Animates [pagerState] to [selectedPage] whenever something other than a swipe changes it, and reports
 * through [onPageChanged] the pages the user moves to.
 *
 * The pages a jump crosses on its way are not reported: they are the jump's own doing, and reported back
 * as a page the user moved to, they would end the jump at the first page it passes over.
 */
@Composable
fun FollowSelectedPage(pagerState: PagerState, selectedPage: () -> Int, onPageChanged: (Int) -> Unit) {
    val currentSelectedPage by rememberUpdatedState(selectedPage)
    val currentOnPageChanged by rememberUpdatedState(onPageChanged)

    var isJumping by remember(pagerState) { mutableStateOf(false) }

    LaunchedEffect(pagerState) {
        snapshotFlow { currentSelectedPage() }.collectLatest { page ->
            if (page == pagerState.currentPage) return@collectLatest

            isJumping = true
            try {
                pagerState.animateScrollToPage(page)
            } finally {
                isJumping = false
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (!isJumping) currentOnPageChanged(page)
        }
    }
}
