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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.di.ViewModelKey
import com.infomaniak.calendar.ui.screen.calendarTest.model.CalendarUi
import com.infomaniak.calendar.ui.screen.calendarTest.utils.toUi
import com.infomaniak.core.common.cancellable
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.account.DavCredentials
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.Calendar
import com.infomaniak.multiplatform_calendar.core.managers.AccountManager
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(CalendarTestViewModel::class)
class CalendarTestViewModel(
    private val accountManager: AccountManager,
    private val calendarManager: CalendarManager,
) : ViewModel() {

    private val accountId = AccountId(1)

    val uiState: StateFlow<CalendarTestUiState>
        field = MutableStateFlow<CalendarTestUiState>(CalendarTestUiState.Loading)

    init {
        observeCalendars()
        initAndSync()
    }

    private fun initAndSync() = viewModelScope.launch {
        val credentials = DavCredentials(
            username = "USERNAME",
            password = "PASSWORD",
        )
        runCatching {
            accountManager.initAccount(accountId, credentials)
            calendarManager.syncCalendars(accountId)
        }.cancellable()
            .onFailure {
                uiState.value = CalendarTestUiState.Error(it.message ?: "Unknown error")
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCalendars() = viewModelScope.launch {
        calendarManager.observeCalendars()
            .flatMapLatest { calendars -> calendars.map(::observeCalendarUi).combineToList() }
            .collect { uiState.value = CalendarTestUiState.Loaded(it) }
    }

    private fun observeCalendarUi(calendar: Calendar): Flow<CalendarUi> =
        calendarManager.observeEvents(calendar.id).map { calendar.toUi(it) }

    private inline fun <reified T> List<Flow<T>>.combineToList(): Flow<List<T>> =
        if (isEmpty()) flowOf(emptyList()) else combine(this) { it.toList() }
}
