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
package com.infomaniak.calendar.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.multiplatform_calendar.core.AccountManager
import com.infomaniak.multiplatform_calendar.core.CalendarManager
import com.infomaniak.multiplatform_calendar.core.data.remote.model.CaldavCredentials
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.AccountId
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Inject
class HomeViewModel(
    private val accountManager: AccountManager,
    private val calendarManager: CalendarManager,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState>
        field = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    private val accountId = AccountId(1)

    init {
        observeCalendars()
        syncFromRemote()
    }

    private fun observeCalendars() {
        viewModelScope.launch {
            calendarManager.observeCalendars(accountId).collect { calendars ->
                Log.d("CalDAV-PoC", "DB updated: ${calendars.size} calendar(s)")
                val message = if (calendars.isEmpty()) {
                    "⏳ Syncing…"
                } else {
                    buildString {
                        appendLine("✅ ${calendars.size} calendar(s) in DB:\n")
                        calendars.forEachIndexed { i, cal ->
                            appendLine("[$i] ${cal.displayName}")
                            appendLine("    ${cal.url}")
                            appendLine()
                        }
                    }
                }
                uiState.value = HomeUiState.Success(message)
            }
        }
    }

    private fun syncFromRemote() {
        viewModelScope.launch {
            try {
                val credentials = CaldavCredentials(
                    baseUrl = "https://sync.infomaniak.com",
                    username = "JA03117",
                    password = "qGTwzESRpAAP0P5M",
                )
                withContext(Dispatchers.IO) {
                    accountManager.initAccount(accountId, credentials)
                    accountManager.syncCalendars(accountId)
                }
                Log.d("CalDAV-PoC", "Sync completed")
            } catch (e: Exception) {
                Log.e("CalDAV-PoC", "Discovery failed", e)
                uiState.value = HomeUiState.Error("❌ Error: ${e.message}")
            }
        }
    }
}
