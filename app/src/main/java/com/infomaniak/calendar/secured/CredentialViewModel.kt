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
package com.infomaniak.calendar.secured

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.di.ViewModelKey
import com.infomaniak.calendar.utils.AccountUtils
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.common.cancellable
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.account.DavCredentials
import com.infomaniak.multiplatform_calendar.core.managers.AccountManager
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class CredentialViewModel(
    private val accountManager: AccountManager,
    private val calendarManager: CalendarManager,
    private val securedDavCredentialsRepository: SecuredDavCredentialsRepository,
    private val accountUtils: AccountUtils,
) : ViewModel() {

    init {
        loadDavCredential()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadDavCredential() {
        viewModelScope.launch {
            accountUtils.users.mapLatest { userList ->
                userList.map { user ->
                    val userId = user.id.toLong()
                    val credentials = securedDavCredentialsRepository.get(userId) ?: user.createAndStoreCredential()
                    AccountId(user.id.toLong()) to credentials
                }
            }.collect { credentialsList ->
                credentialsList.forEach { (accountId, credentials) ->
                    runCatching {
                        accountManager.initAccount(accountId, credentials)
                        calendarManager.syncCalendars(accountId)
                    }.cancellable()
                }
            }
        }
    }

    private suspend fun User.createAndStoreCredential(): DavCredentials {
        val credentials = accountManager.retrieveDavCredential(authToken = apiToken.accessToken, login = login)
        securedDavCredentialsRepository.save(userId = id.toLong(), credential = credentials)
        return credentials
    }
}
