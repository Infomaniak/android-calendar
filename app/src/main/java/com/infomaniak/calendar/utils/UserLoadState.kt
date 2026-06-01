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
package com.infomaniak.calendar.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.infomaniak.core.auth.models.user.User
import kotlinx.coroutines.flow.map

sealed interface UserLoadState {
    object Loading : UserLoadState
    data class Loaded(val user: User?) : UserLoadState
}

@Composable
fun AccountUtils.rememberUserLoadState(): State<UserLoadState> {
    val userLoadStateFlow = remember { currentUserFlow.map(UserLoadState::Loaded) }
    return userLoadStateFlow.collectAsStateWithLifecycle(initialValue = UserLoadState.Loading)
}
