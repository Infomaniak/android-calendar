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

import com.infomaniak.core.auth.models.user.User
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.account.DavCredentials
import com.infomaniak.multiplatform_calendar.core.managers.AccountManager
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

/**
 * Manager to sync accounts with KMP.
 *
 * Credentials are transparently encrypted and decrypted by [CalendarDataValues] via its serializer.
 * Accounts are initialized via [AccountManager] when credentials are saved or loaded at startup.
 */
class DavCredentialsManager @Inject constructor(
    private val dataValues: CalendarDataValues,
    private val accountManager: AccountManager,
) {
    suspend fun initStoredCredentials() {
        dataValues.davCredentials.flow.first().forEach { (userId, credentials) ->
            initAccountCredential(AccountId(userId), credentials)
        }
    }

    suspend fun addCredential(user: User, davCredentials: DavCredentials) {
        val userId = user.id.toLong()
        dataValues.davCredentials.update { current -> current + (userId to davCredentials) }
        initAccountCredential(AccountId(userId), davCredentials)
    }

    suspend fun initAccountCredential(accountId: AccountId, davCredentials: DavCredentials) {
        accountManager.initAccount(accountId, davCredentials)
    }

    suspend fun removeCredential(userId: Long) {
        dataValues.davCredentials.update { it - userId }
        accountManager.removeAccount(AccountId(userId))
    }
}
