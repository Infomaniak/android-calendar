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
package com.infomaniak.calendar

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.infomaniak.calendar.data.CalendarDataValues
import com.infomaniak.calendar.manager.SyncEventsManager
import com.infomaniak.calendar.manager.VisibleDateManager
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.calendar.utils.account.accountId
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.common.cancellable
import com.infomaniak.multiplatform_core.account.domain.model.AccountId
import com.infomaniak.multiplatform_core.contacts.ContactsManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@AssistedInject
class MainViewModel(
    @Assisted savedStateHandle: SavedStateHandle,
    private val accountUtils: AccountUtils,
    private val syncEventsManager: SyncEventsManager,
    private val calendarDataValues: CalendarDataValues,
    private val contactsManager: ContactsManager,
    visibleDateManager: VisibleDateManager,
) : ViewModel() {
    val loadingEventsError: ReceiveChannel<SyncEventsManager.SyncError> = syncEventsManager.loadingError

    val uiState: StateFlow<MainUiState> = combine(
        accountUtils.users,
        calendarDataValues.lastCalendarView.flow,
    ) { users, lastCalendarView ->
        MainUiState.Loaded(isUserConnected = users.isNotEmpty(), lastCalendarView = lastCalendarView)
    }.stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = MainUiState.Loading)

    val visibleDate = visibleDateManager.visibleDate

    private val contactsSyncRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        visibleDateManager.bindTo(savedStateHandle)
        syncEventsForConnectedUsers()
        syncContactsForConnectedAccounts()
    }

    fun saveCalendarView(calendarView: NavDestination.CalendarView) {
        viewModelScope.launch { calendarDataValues.lastCalendarView.setValue(calendarView) }
    }

    private fun syncEventsForConnectedUsers() {
        var previousUserIds = emptySet<Int>()

        viewModelScope.launch {
            accountUtils.users
                .map { users -> users.mapTo(mutableSetOf(), User::id) }
                .distinctUntilChanged()
                .collectLatest { userIds ->
                    val hasNewUser = !previousUserIds.containsAll(userIds)
                    previousUserIds = userIds
                    if (!hasNewUser) return@collectLatest

                    syncEventsManager.loadCurrentMonths(visibleDate = visibleDate.value)
                }
        }
    }

    fun syncContacts() {
        contactsSyncRequests.tryEmit(Unit)
    }

    // An account change resets the cooldown, so it syncs right away whatever the last sync time was.
    private fun syncContactsForConnectedAccounts() = viewModelScope.launch {
        accountUtils.users
            .map { users -> users.mapTo(mutableSetOf(), User::accountId) }
            .distinctUntilChanged()
            .collectLatest { accountIds ->
                contactsSyncRequests
                    .onStart { emit(Unit) } // Whenever accounts update, sync contacts right away by emitting a request.
                    .discardOnCooldown(SYNC_CONTACTS_MIN_INTERVAL)
                    .collect { syncContacts(accountIds) }
            }
    }

    private suspend fun syncContacts(accountIds: MutableSet<AccountId>) {
        runCatching {
            contactsManager.sync(accountIds)
        }.cancellable().onFailure { throwable ->
            Log.w(TAG, "Failed to sync contacts", throwable)
        }
    }

    @AssistedFactory
    @ViewModelAssistedFactoryKey(MainViewModel::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): MainViewModel = create(extras.createSavedStateHandle())

        fun create(@Assisted savedStateHandle: SavedStateHandle): MainViewModel
    }

    companion object {
        private const val TAG = "MainViewModel"
        private val SYNC_CONTACTS_MIN_INTERVAL = 1.minutes
    }
}

/** Emits a value only if [cooldown] has elapsed since the last emitted one, dropping the others. */
private fun <T> Flow<T>.discardOnCooldown(cooldown: Duration): Flow<T> = flow {
    var lastEmission: TimeMark? = null
    collect { value ->
        if (lastEmission?.let { it.elapsedNow() < cooldown } == true) return@collect
        lastEmission = TimeSource.Monotonic.markNow()
        emit(value)
    }
}
