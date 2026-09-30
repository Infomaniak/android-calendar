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
package com.infomaniak.calendar.ui.screen.week

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.calendar.component.ExpandableCalendar
import com.infomaniak.calendar.components.calendar.component.collapseCalendarOnScroll
import com.infomaniak.calendar.components.calendar.component.rememberCalendarExpansionState
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.ui.component.OverlaidTopBarScaffold
import com.infomaniak.calendar.ui.component.topAppBar.CalendarTopAppBar
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.datetime.LocalDate

@Composable
fun WeekScreen(modifier: Modifier = Modifier, viewModel: WeekScreenViewModel = viewModel()) {
    val isLoadingEvents by viewModel.isLoadingEvents.collectAsStateWithLifecycle(initialValue = false)
    val eventDots by viewModel.eventDots.collectAsStateWithLifecycle()

    WeekScreen(
        isLoadingEvents = { isLoadingEvents },
        eventsDots = { eventDots },
        modifier = modifier,
    )
}

@Composable
private fun WeekScreen(
    isLoadingEvents: () -> Boolean,
    eventsDots: () -> Map<LocalDate, List<EventColorsUi>>,
    modifier: Modifier = Modifier,
) {
    val calendarExpansionState = rememberCalendarExpansionState()
    val hazeState = rememberHazeState()
    val visibleDayState = LocalVisibleDayState.current ?: return

    OverlaidTopBarScaffold(
        topBar = {
            CalendarTopAppBar(
                isLoadingEvents = isLoadingEvents,
                onToggleCalendar = calendarExpansionState::toggle,
                calendarExpansionProgress = { calendarExpansionState.progress },
                hazeState = hazeState,
                calendar = {
                    ExpandableCalendar(
                        expansionState = calendarExpansionState,
                        selectedDate = { visibleDayState.visibleDate },
                        onDayClick = { visibleDayState.jumpTo(it) },
                        weekNumbering = WeekNumbering.ISO_8601, //TODO[weekNumbering]: Use week numbering from LocalSettings
                        eventsDots = eventsDots,
                    )
                },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .collapseCalendarOnScroll(calendarExpansionState),
        ) {
            
        }
    }
}

@Preview
@Composable
private fun WeekScreenPreview() {
    CalendarThemeForPreview {
        WeekScreen()
    }
}
