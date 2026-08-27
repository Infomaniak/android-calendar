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
package com.infomaniak.calendar.ui.screen.accounts

import androidx.lifecycle.ViewModel
import com.infomaniak.calendar.utils.account.AccountUtils
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.first

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class AccountsViewModel(
    private val accountUtils: AccountUtils,
) : ViewModel() {

    // Returns true if there are other users left after removing the current user.
    // If there are, we need to call onBack after removing the user. If there isn't,
    // the app will handle the case where there isn't any user left.
    suspend fun removeUser(userId: Int): Boolean {
        accountUtils.removeUser(userId)
        return accountUtils.users.first().isNotEmpty()
    }
}
