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
import com.infomaniak.calendar.ui.screen.calendarTest.utils.toPlanningWeeks
import com.infomaniak.calendar.utils.AccountUtils
import com.infomaniak.core.common.cancellable
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.managers.AccountManager
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(CalendarTestViewModel::class)
class CalendarTestViewModel(
    private val accountManager: AccountManager,
    private val accountUtils: AccountUtils,
    private val calendarManager: CalendarManager,
) : ViewModel() {

    val uiState: StateFlow<CalendarTestUiState>
        field = MutableStateFlow<CalendarTestUiState>(CalendarTestUiState.Loading)

    val userFlow = accountUtils.currentUserFlow.filterNotNull()

    @OptIn(ExperimentalTime::class)
    private val rangeFlow = MutableStateFlow(initialRange())

    fun processAction(action: CalendarTestAction) = when (action) {
        is CalendarTestAction.OnClickDisconnect -> onClickDisconnect()
        is CalendarTestAction.LoadMorePast -> loadMorePast()
        is CalendarTestAction.LoadMoreFuture -> loadMoreFuture()
    }

    init {
        initAndSync()
        observePlanning()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initAndSync() = viewModelScope.launch {
        userFlow.mapLatest { user ->
            AccountId(user.id.toLong()) to
                    accountManager.retrieveDavCredential(user.apiToken.accessToken, user.login)
        }.collect { (accountId, credentials) ->
            runCatching {
                accountManager.initAccount(accountId, credentials)
                calendarManager.syncCalendars(accountId)
            }.cancellable()
                .onFailure {
                    uiState.value = CalendarTestUiState.Error(it.message ?: "Unknown error")
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
    private fun observePlanning() = viewModelScope.launch {
        rangeFlow.flatMapLatest { range ->
            calendarManager.observeEvents(range.start, range.end).map { events ->
                events.toPlanningWeeks(range.start, range.end)
            }
        }.collect { weeks ->
            uiState.value = CalendarTestUiState.Loaded(weeks)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun loadMorePast() = rangeFlow.update { range -> range.copy(start = range.start - RANGE_CHUNK) }

    @OptIn(ExperimentalTime::class)
    private fun loadMoreFuture() = rangeFlow.update { range -> range.copy(end = range.end + RANGE_CHUNK) }

    private fun onClickDisconnect() {
        viewModelScope.launch {
            val userId = userFlow.map { it.id }.first()
            accountUtils.removeUser(userId)
            accountManager.removeAccount(AccountId(userId.toLong()))
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun initialRange(): PlanningRange {
        // TODO: Timezones are not handled yet — the planning range is computed in UTC.
        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        val start = today.atStartOfDayIn(TimeZone.UTC)
        return PlanningRange(start = start, end = start + RANGE_CHUNK)
    }

    @OptIn(ExperimentalTime::class)
    private data class PlanningRange(val start: Instant, val end: Instant)

    private companion object {
        // Must stay large enough that even an empty chunk adds more week separators than
        // Planning's LOAD_MORE_BUFFER, otherwise infinite scroll stalls over event-less periods.
        val RANGE_CHUNK = 120.days
    }
}
