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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.paging.compose.LazyPagingItems
import com.infomaniak.calendar.components.planning.PlanningRow
import com.infomaniak.calendar.ui.state.VisibleDayState
import kotlinx.datetime.LocalDate

/**
 * Positions the planning on [VisibleDayState.visibleDate] **once** per screen lifetime, when the
 * first page becomes available.
 *
 * Routing through [onJumpTo] recenters the pager when the (possibly restored) visible date is outside
 * the initial today-centered window; it's a no-op when the date is already the current center
 * (the `initialDay` `StateFlow` dedups equal values).
 *
 * The `rememberSaveable` guard makes sure a later recomposition, configuration change or process
 * restoration never re-centers over the user's (or restored) scroll — explicit jumps still go through
 * [ProcessJumpRequests].
 */
@Composable
fun CenterOnVisibleDate(
    lazyListState: LazyListState,
    planningRows: LazyPagingItems<PlanningRow>,
    visibleDayState: VisibleDayState,
    onJumpTo: (LocalDate) -> Boolean,
) {
    var hasCentered by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(planningRows) {
        if (hasCentered) return@LaunchedEffect

        planningRows.jumpToDate(lazyListState, visibleDayState.visibleDate, onJumpTo)

        hasCentered = true
    }
}
