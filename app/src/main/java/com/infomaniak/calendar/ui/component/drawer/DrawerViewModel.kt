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
import com.infomaniak.calendar.ui.component.drawer.model.CalendarUi
import com.infomaniak.calendar.ui.component.drawer.model.UserCalendarsUi
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.calendar.utils.account.accountId
import com.infomaniak.calendar.utils.toCalendarColorsUi
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.Calendar
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarEditData
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class DrawerViewModel(accountUtils: AccountUtils, private val calendarManager: CalendarManager) : ViewModel() {
    val calendarsUsers: StateFlow<List<UserCalendarsUi>> = combine(
        accountUtils.users,
        calendarManager.observeCalendars(),
    ) { users, calendars ->
        return@combine users.map { user ->
            val userCalendars = calendars.filter { it.accountId == user.accountId }.map { it.toCalendarUi() }
            UserCalendarsUi(user, userCalendars)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList(),
    )

    fun onCalendarVisibilityChanged(calendarId: CalendarId, isVisible: Boolean) {
        viewModelScope.launch {
            calendarManager.updateCalendar(calendarId, edit = CalendarEditData(isVisible = isVisible))
        }
    }
}

private fun Calendar.toCalendarUi(): CalendarUi = CalendarUi(
    id = id,
    accountId = accountId,
    displayName = displayName,
    colors = colors.toCalendarColorsUi(),
    isVisible = isVisible,
)
