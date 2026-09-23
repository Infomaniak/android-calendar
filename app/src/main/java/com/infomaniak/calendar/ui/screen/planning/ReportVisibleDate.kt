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

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.infomaniak.calendar.components.planning.PlanningItemKey
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
fun ReportVisibleDate(lazyListState: LazyListState, onVisibleDateChanged: (LocalDate) -> Unit) {
    LaunchedEffect(lazyListState) {
        var isUserScroll = false
        var hasScrolled = false
        var lastReportedDate: LocalDate? = null

        fun reportVisibleDate(date: LocalDate?) {
            if (date == null || date == lastReportedDate) return

            onVisibleDateChanged(date)
            lastReportedDate = date
        }

        launch {
            lazyListState.interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is DragInteraction.Start -> {
                        isUserScroll = true
                        hasScrolled = lazyListState.isScrollInProgress
                    }
                    is DragInteraction.Stop, is DragInteraction.Cancel -> {
                        if (!hasScrolled) isUserScroll = false
                    }
                    else -> Unit
                }
            }
        }

        snapshotFlow { lazyListState.isScrollInProgress to lazyListState.firstVisibleDate() }.collect { (isScrolling, date) ->
            if (isScrolling && isUserScroll) {
                hasScrolled = true
                reportVisibleDate(date)
            } else if (!isScrolling && isUserScroll && hasScrolled) {
                reportVisibleDate(date)
                isUserScroll = false
                hasScrolled = false
            }
        }
    }
}

private fun LazyListState.firstVisibleDate(): LocalDate? {
    return (layoutInfo.visibleItemsInfo.firstOrNull { it.offset + it.size > 0 }?.key as? PlanningItemKey)?.date
}
