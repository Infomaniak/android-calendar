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
import com.infomaniak.multiplatform_calendar.data.remote.RustCaldavBridge
import com.infomaniak.multiplatform_calendar.data.repository.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    val uiState: StateFlow<HomeUiState>
        field = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    private val calendarRepository = CalendarRepository(caldavClient = RustCaldavBridge)

    init {
        retrieveCalendars()
    }

    fun retrieveCalendars() {
        viewModelScope.launch {
            try {
                val calendars = withContext(Dispatchers.IO) {
                    calendarRepository.discoverCalendars(
                        baseUrl = "https://sync.infomaniak.com",
                        username = "USERNAME",
                        password = "PASSWORD",
                    )
                }
                Log.d("CalDAV-PoC", "Found ${calendars.size} calendar(s)")
                calendars.forEachIndexed { i, cal ->
                    Log.d("CalDAV-PoC", "  [$i] ${cal.displayName}  →  ${cal.url}")
                }
                val message = if (calendars.isEmpty()) {
                    "✅ Connected — but 0 calendars found."
                } else {
                    buildString {
                        appendLine("✅ Found ${calendars.size} calendar(s):\n")
                        calendars.forEachIndexed { i, cal ->
                            appendLine("[$i] ${cal.displayName}")
                            appendLine("    ${cal.url}")
                            appendLine()
                        }
                    }
                }
                uiState.value = HomeUiState.Success(message)
            } catch (e: Exception) {
                Log.e("CalDAV-PoC", "Discovery failed", e)
                uiState.value = HomeUiState.Error("❌ Error: ${e.message}")
            }
        }
    }
}
