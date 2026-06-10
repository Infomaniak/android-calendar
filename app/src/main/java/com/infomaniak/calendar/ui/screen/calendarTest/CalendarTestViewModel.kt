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
package com.infomaniak.calendar.ui.screen.calendarTest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.di.ViewModelKey
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.account.DavCredentials
import com.infomaniak.multiplatform_calendar.core.managers.AccountManager
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(CalendarTestViewModel::class)
class CalendarTestViewModel(
    private val accountManager: AccountManager,
    private val calendarManager: CalendarManager,
) : ViewModel() {

    val uiState: StateFlow<CalendarTestUiState>
        field = MutableStateFlow<CalendarTestUiState>(CalendarTestUiState.Loading)

    private val accountId = AccountId(1)

    init {
        observeCalendars()
        initCalendar()
        syncFromRemote()
    }

    private fun observeCalendars() = viewModelScope.launch {
        calendarManager.observeCalendars().collect { calendars ->
            Log.d("CalDAV-PoC", "DB updated: ${calendars.size} calendar(s)")
            if (calendars.isNotEmpty()) {
                uiState.value = CalendarTestUiState.Success(
                    buildString {
                        appendLine("✅ ${calendars.size} calendar(s) in DB:\n")
                        calendars.forEachIndexed { i, cal ->
                            appendLine("[$i] ${cal.displayName}")
                            appendLine("    ${cal.id}")
                            appendLine()
                        }
                    },
                )
            }
        }
    }

    private fun initCalendar() = viewModelScope.launch {
        val credentials = DavCredentials(
            username = "USERNAME",
            password = "PASSWORD",
        )
        accountManager.initAccount(accountId, credentials)
    }

    private fun syncFromRemote() = viewModelScope.launch {
        runCatching { calendarManager.syncCalendars(accountId) }
            .onFailure { uiState.value = CalendarTestUiState.Error(it.message ?: "Error") }
    }

}
