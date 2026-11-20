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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.infomaniak.calendar.components.day.model.DayPages
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate

/**
 * Keeps [pagerState] on the page holding [selectedDate], and reports the first day of the page the
 * user settles on through [onVisibleDateChanged], unless that page already holds [selectedDate].
 */
@Composable
internal fun FollowSelectedDate(
    pagerState: PagerState,
    pages: DayPages,
    selectedDate: () -> LocalDate,
    onVisibleDateChanged: (LocalDate) -> Unit,
) {
    // The page a jump is travelling to, while it travels. The pager reports every page it crosses on
    // the way, and those are the jump's own doing: reported back as a date the user moved to, they
    // would end the jump at the first page it passes over.
    var travellingTo by remember(pagerState) { mutableStateOf<Int?>(null) }

    LaunchedEffect(pagerState) {
        snapshotFlow { selectedDate() }.collectLatest { date ->
            val page = pages.pageOf(date)
            if (page == pagerState.currentPage) return@collectLatest

            travellingTo = page
            try {
                pagerState.animateScrollToPage(page)
            } finally {
                travellingTo = null
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (travellingTo == null && pages.pageOf(selectedDate()) != page) onVisibleDateChanged(pages.firstDateOf(page))
        }
    }
}
