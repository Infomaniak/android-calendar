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
import androidx.compose.foundation.interaction.Interaction
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
        val reporter = VisibleDateReporter()

        launch {
            lazyListState.interactionSource.interactions.collect { interaction ->
                reporter.onInteraction(interaction, lazyListState.isScrollInProgress)
            }
        }

        snapshotFlow { lazyListState.isScrollInProgress to lazyListState.firstVisibleDate() }.collect { (isScrolling, date) ->
            reporter.onScrollStateChanged(isScrolling, date, onVisibleDateChanged)
        }
    }
}

private class VisibleDateReporter {
    private var isUserScroll = false
    private var hasScrolled = false
    private var lastReportedDate: LocalDate? = null

    fun onInteraction(interaction: Interaction, isScrollInProgress: Boolean) {
        when (interaction) {
            is DragInteraction.Start -> {
                isUserScroll = true
                hasScrolled = isScrollInProgress
            }
            is DragInteraction.Stop, is DragInteraction.Cancel -> clearUserScrollIfNotScrolled()
            else -> Unit
        }
    }

    fun onScrollStateChanged(isScrolling: Boolean, date: LocalDate?, onVisibleDateChanged: (LocalDate) -> Unit) {
        if (isScrolling && isUserScroll) {
            hasScrolled = true
            reportVisibleDate(date, onVisibleDateChanged)
            return
        }

        if (!isScrolling && isUserScroll && hasScrolled) {
            reportVisibleDate(date, onVisibleDateChanged)
            isUserScroll = false
            hasScrolled = false
        }
    }

    private fun clearUserScrollIfNotScrolled() {
        if (!hasScrolled) isUserScroll = false
    }

    private fun reportVisibleDate(date: LocalDate?, onVisibleDateChanged: (LocalDate) -> Unit) {
        if (date == null || date == lastReportedDate) return

        onVisibleDateChanged(date)
        lastReportedDate = date
    }
}

private fun LazyListState.firstVisibleDate(): LocalDate? {
    return (layoutInfo.visibleItemsInfo.firstOrNull { it.offset + it.size > 0 }?.key as? PlanningItemKey)?.date
}
