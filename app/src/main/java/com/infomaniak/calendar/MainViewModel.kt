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

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.infomaniak.calendar.di.ViewModelAssistedFactory
import com.infomaniak.calendar.di.ViewModelAssistedFactoryKey
import com.infomaniak.calendar.manager.SyncEventsManager
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.common.utils.today
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

@AssistedInject
class MainViewModel(
    @Assisted savedStateHandle: SavedStateHandle,
    private val accountUtils: AccountUtils,
    private val syncEventsManager: SyncEventsManager,
) : ViewModel() {
    val loadingEventsError: ReceiveChannel<SyncEventsManager.SyncError> = syncEventsManager.loadingError

    @OptIn(SavedStateHandleSaveableApi::class)
    val visibleDay: MutableState<LocalDate> = savedStateHandle.saveable("visibleDay") {
        mutableStateOf(Clock.today())
    }

    init {
        syncEventsForConnectedUsers()
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

                    syncEventsManager.loadCurrentMonths(visibleDate = visibleDay.value)
                }
        }
    }

    @AssistedFactory
    @ViewModelAssistedFactoryKey(MainViewModel::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): MainViewModel = create(extras.createSavedStateHandle())

        fun create(@Assisted savedStateHandle: SavedStateHandle): MainViewModel
    }
}
