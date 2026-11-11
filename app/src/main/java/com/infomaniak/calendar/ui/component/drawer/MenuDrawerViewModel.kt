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
package com.infomaniak.calendar.ui.component.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.BuildConfig
import com.infomaniak.calendar.data.CalendarDataValues
import com.infomaniak.calendar.manager.CachedCalendarManager
import com.infomaniak.calendar.ui.model.UserCalendarsUi
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class MenuDrawerViewModel(
    cachedCalendarManager: CachedCalendarManager,
    private val calendarManager: CalendarManager,
    private val calendarDataValues: CalendarDataValues,
) : ViewModel() {
    val calendarsUsers: StateFlow<List<UserCalendarsUi>> = cachedCalendarManager.usersCalendars

    val expandedAccountIds: Flow<Set<Int>> = calendarDataValues.expandedDrawerAccountIds.flow

    val collapsedSectionKeys: Flow<Set<String>> = calendarDataValues.collapsedDrawerSectionKeys.flow

    fun onCalendarVisibilityChanged(calendarId: CalendarId, isVisible: Boolean) {
        viewModelScope.launch {
            calendarManager.updateCalendarVisibility(calendarId, isVisible)
        }
    }

    fun onCalendarLongClicked(selectedId: CalendarId) {
        if (BuildConfig.DEBUG) viewModelScope.launch { calendarManager.showOnlyCalendar(selectedId) }
    }

    fun onAccountExpandedChanged(userId: Int, isExpanded: Boolean) {
        viewModelScope.launch {
            calendarDataValues.expandedDrawerAccountIds.update { current ->
                if (isExpanded) current + userId else current - userId
            }
        }
    }

    fun onSectionExpandedChanged(sectionKey: String, isExpanded: Boolean) {
        viewModelScope.launch {
            calendarDataValues.collapsedDrawerSectionKeys.update { current ->
                if (isExpanded) current - sectionKey else current + sectionKey
            }
        }
    }
}
