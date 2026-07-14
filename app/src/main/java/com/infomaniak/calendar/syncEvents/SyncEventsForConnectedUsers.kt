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
package com.infomaniak.calendar.syncEvents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.staticCompositionLocalOf
import com.infomaniak.calendar.di.ComposeAppGraph
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.core.auth.models.user.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

val LocalLoadingEventsState = staticCompositionLocalOf<MutableState<Boolean>?> { null }

@Composable
fun SyncEventsForConnectedUsers(
    accountUtils: AccountUtils = ComposeAppGraph.accountUtils,
    syncEventsManager: SyncEventsManager = ComposeAppGraph.syncEventsManager,
) {
    val visibleDayState = LocalVisibleDayState.current ?: return
    val loadingEventsState = LocalLoadingEventsState.current ?: return

    LaunchedEffect(Unit) {
        var previousUserIds = emptySet<Int>()

        accountUtils.users
            .map { it.mapTo(mutableSetOf(), User::id) }
            .distinctUntilChanged()
            .collectLatest { userIds ->
                val savedPreviousUserIds = previousUserIds
                previousUserIds = userIds

                // If no new user has been added
                if (savedPreviousUserIds.containsAll(userIds)) return@collectLatest

                syncEventsManager.loadCurrentMonths(
                    visibleDate = visibleDayState.visibleDate,
                    onLoadingChanged = { isLoading -> loadingEventsState.value = isLoading },
                )
            }
    }
}
