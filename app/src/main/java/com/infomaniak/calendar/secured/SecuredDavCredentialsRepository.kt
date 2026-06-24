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
 * Repository responsible for the secure persistence of DAV credentials, keyed by user ID.
 *
 * Credentials are transparently encrypted and decrypted by [CalendarDataValues] via its serializer.
 * Accounts are initialized via [AccountManager] when credentials are saved or loaded at startup.
 */
class SecuredDavCredentialsRepository @Inject constructor(
    private val dataValues: CalendarDataValues,
    private val accountManager: AccountManager,
) {
    /**
     * Loads all persisted DAV credentials and initializes each account.
     *
     * Intended to be called once at app startup. Credentials are decrypted
     * transparently by [CalendarDataValues].
     */
    suspend fun loadAndInitAllCredentials() {
        dataValues.davCredentials.flow.first().forEach { (userId, credentials) ->
            initAndLoadDavCredential(AccountId(userId), credentials)
        }
    }

    /**
     * Persists the given DAV credentials locally (encrypted transparently) and initializes the account.
     *
     * Operations are performed sequentially:
     * 1. Persist the credentials in [CalendarDataValues].
     * 2. Initialize the account via [initAndLoadDavCredential].
     *
     * Any existing credentials for this user are replaced.
     *
     * @param user The authenticated user whose DAV credentials should be stored.
     * @param davCredentials Plain-text credentials to persist and pass to [AccountManager].
     */
    suspend fun persistAndInitDavCredentials(user: User, davCredentials: DavCredentials) {
        val userId = user.id.toLong()
        dataValues.davCredentials.update { current -> current + (userId to davCredentials) }
        initAndLoadDavCredential(AccountId(userId), davCredentials)
    }

    /**
     * Initializes a DAV account.
     *
     * Intended for accounts whose credentials are already known — either freshly
     * fetched via [persistAndInitDavCredentials] or loaded from storage.
     *
     * @param accountId The account to initialize.
     * @param davCredentials Plain-text credentials to pass to [AccountManager].
     */
    suspend fun initAndLoadDavCredential(accountId: AccountId, davCredentials: DavCredentials) {
        accountManager.initAccount(accountId, davCredentials)
    }

    /**
     * Removes the DAV credentials for the given user from persistent storage.
     *
     * @param userId Unique identifier of the user.
     */
    suspend fun remove(userId: Long) {
        dataValues.davCredentials.update { it - userId }
    }
}
