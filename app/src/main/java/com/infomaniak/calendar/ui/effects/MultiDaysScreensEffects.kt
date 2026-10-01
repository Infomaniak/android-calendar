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
package com.infomaniak.calendar.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.Dp
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.foundation.state.VisibleDayState
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filterNot
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth

/**
 * Stores the zoom level the user leaves the day view on.
 *
 * A pinch changes the height on every frame, so the height is stored once the gesture ends: one
 * write per pinch, and nothing left waiting on a timer that leaving the screen would cancel.
 */
@Composable
fun SaveHourHeight(timelineState: DayTimelineState, onHourHeightChanged: suspend (Dp) -> Unit) {
    LaunchedEffect(timelineState) {
        snapshotFlow { timelineState.isPinching }
            .dropWhile { isPinching -> !isPinching }
            .filterNot { isPinching -> isPinching }
            .collect { onHourHeightChanged(timelineState.hourHeight) }
    }
}

/**
 * Turns a jump request, such as tapping a day in the calendar, into a change of visible date. The
 * pager then animates to it on its own, since it follows the visible date.
 */
@Composable
fun ApplyJumpRequests(visibleDayState: VisibleDayState) {
    LaunchedEffect(visibleDayState) {
        for (date in visibleDayState.jumpCommand) visibleDayState.updateVisibleDate(date)
    }
}

@Composable
fun ReportVisibleMonth(visibleDayState: VisibleDayState, onVisibleMonthChanged: (YearMonth) -> Unit) {
    val currentOnVisibleMonthChanged by rememberUpdatedState(onVisibleMonthChanged)

    LaunchedEffect(visibleDayState) {
        snapshotFlow { visibleDayState.visibleDate.yearMonth }.collect { currentOnVisibleMonthChanged(it) }
    }
}
