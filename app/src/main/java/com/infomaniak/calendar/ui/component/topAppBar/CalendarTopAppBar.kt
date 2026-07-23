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
package com.infomaniak.calendar.ui.component.topAppBar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.ui.modifier.backgroundBlur
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.state.VisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import dev.chrisbanes.haze.HazeState
import kotlinx.datetime.LocalDate

@Composable
fun CalendarTopAppBar(
    onToggleCalendar: () -> Unit,
    isLoadingEvents: () -> Boolean,
    isCalendarExpanded: () -> Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    calendar: @Composable () -> Unit = {},
) {
    val containerColor = TopAppBarDefaults.topAppBarColors().containerColor
    Column(modifier = modifier.backgroundBlur(containerColor, hazeState)) {
        TopAppBar(
            title = {
                CurrentMonthTitle(isExpanded = isCalendarExpanded, onClick = onToggleCalendar, modifier = Modifier.fillMaxWidth())
            },
            navigationIcon = { TopAppBarButtons.DrawerIconButton() },
            actions = {
                TopAppBarButtons.InboxButton(onClick = {})
                TopAppBarButtons.SearchButton(onClick = {})
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
        calendar.invoke()
        LoadingEventsIndicator(isLoading = isLoadingEvents)
    }
}

@Preview
@Composable
private fun CalendarTopAppBarPreview() {
    CalendarThemeForPreview {
        val visibleDate = remember { mutableStateOf(LocalDate(2026, 7, 8)) }
        CompositionLocalProvider(
            LocalVisibleDayState provides VisibleDayState(_visibleDate = visibleDate),
        ) {
            CalendarTopAppBar(
                onToggleCalendar = {},
                isLoadingEvents = { true },
                isCalendarExpanded = { false },
                hazeState = HazeState(),
            )
        }
    }
}
