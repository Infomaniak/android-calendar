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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.infomaniak.calendar.ui.state.CurrentDayState
import kotlin.math.abs

@Composable
fun ObserveScrollAction(
    currentDay: CurrentDayState,
    firstVisibleItemIndex: () -> Int,
    eventsByWeekAndDay: () -> EventsByWeekAndDay,
    animatedScroll: suspend (index: Int) -> Unit,
    instantScroll: suspend (index: Int) -> Unit,
) {
    LaunchedEffect(currentDay) {
        currentDay.scrollCommand.collect { date ->
            val targetIndex = eventsByWeekAndDay().indexOf(date)
            val distance = abs(targetIndex - firstVisibleItemIndex())

            runCatching {
                if (distance > SCROLL_THRESHOLD) instantScroll(targetIndex) else animatedScroll(targetIndex)
            }
        }
    }
}

private const val SCROLL_THRESHOLD = 50
