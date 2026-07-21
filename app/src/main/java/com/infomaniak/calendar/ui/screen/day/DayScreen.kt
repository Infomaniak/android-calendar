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
package com.infomaniak.calendar.ui.screen.day

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.calendar.component.ExpandableCalendar
import com.infomaniak.calendar.ui.LocalUser
import com.infomaniak.calendar.ui.component.topAppBar.CalendarTopAppBar
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.state.VisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.utils.today
import dev.chrisbanes.haze.rememberHazeState
import kotlin.time.Clock

@Composable
fun DayScreen(
    modifier: Modifier = Modifier,
    dayViewModel: DayViewModel = viewModel(),
) {
    val isLoadingEvents by dayViewModel.isLoadingEvents.collectAsStateWithLifecycle(initialValue = false)

    DayScreen(
        modifier = modifier,
        isLoadingEvents = { isLoadingEvents },
    )
}

@Composable
private fun DayScreen(isLoadingEvents: () -> Boolean, modifier: Modifier = Modifier) {
    val hazeState = rememberHazeState()
    var isCalendarExpanded by rememberSaveable { mutableStateOf(false) }
    val visibleDayState = LocalVisibleDayState.current

    Scaffold(
        topBar = {
            CalendarTopAppBar(
                isLoadingEvents = isLoadingEvents,
                hazeState = hazeState,
                onToggleCalendar = { isCalendarExpanded = !isCalendarExpanded },
                isCalendarExpanded = { isCalendarExpanded },
                expandableCalendar = visibleDayState?.let { dayState ->
                    {
                        ExpandableCalendar(
                            isExpanded = { isCalendarExpanded },
                            selectedDate = { dayState.visibleDate },
                            onDayClick = dayState::jumpTo,
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("DayScreenContent")
            Text("Current user: ${LocalUser.current?.displayName}")
        }
    }
}

@Preview
@Composable
private fun DayScreenPreview() {
    CalendarThemeForPreview {
        val visibleDate = remember { mutableStateOf(Clock.today()) }

        CompositionLocalProvider(LocalVisibleDayState provides VisibleDayState(visibleDate)) {
            DayScreen(isLoadingEvents = { true })
        }
    }
}
