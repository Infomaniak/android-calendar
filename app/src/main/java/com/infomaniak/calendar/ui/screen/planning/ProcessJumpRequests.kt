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
import androidx.compose.runtime.rememberUpdatedState
import com.infomaniak.calendar.ui.state.VisibleDayState
import kotlin.math.abs

@Composable
fun ProcessJumpRequests(lazyListState: LazyListState, visibleDayState: VisibleDayState, events: () -> EventsByWeekAndDay) {
    val currentEventsByWeekAndDay by rememberUpdatedState(events)

    LaunchedEffect(visibleDayState) {
        visibleDayState.scrollCommand.collect { date ->
            val targetIndex = currentEventsByWeekAndDay().indexOf(date)
            val distance = abs(targetIndex - lazyListState.firstVisibleItemIndex)

            // Swallow CancellationException thrown when a user gesture interrupts the programmatic scroll.
            runCatching {
                if (distance > SCROLL_THRESHOLD) {
                    lazyListState.scrollToItem(targetIndex)
                } else {
                    lazyListState.animateScrollToItem(targetIndex)
                }
            }
        }
    }
}

private const val SCROLL_THRESHOLD = 50
