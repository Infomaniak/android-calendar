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
 * Credentials are transparently encrypted and decrypted by [CalendarDataValues] via its serializer
 * DAV credentials are fetched from the server through [AccountManager] during [saveAndInitDavCredentials],
 * which also triggers account initialization.
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
     * Fetches DAV credentials from the server, persists them locally (encrypted transparently),
     * then initializes the account.
     *
     * Operations are performed sequentially:
     * 1. Retrieve plain-text credentials from the server via [AccountManager].
     * 2. Persist the credentials in [CalendarDataValues].
     * 3. Initialize the account via [initAndLoadDavCredential].
     *
     * Any existing credentials for this user are replaced.
     *
     * @param user The authenticated user whose DAV credentials should be fetched and stored.
     */
    suspend fun saveAndInitDavCredentials(user: User, davCredentials: DavCredentials) {
        val userId = user.id.toLong()
        dataValues.davCredentials.update { current -> current + (userId to davCredentials) }
        initAndLoadDavCredential(AccountId(userId), davCredentials)
    }

    /**
     * Initializes a DAV account.
     *
     * Intended for accounts whose credentials are already known — either freshly
     * fetched via [saveAndInitDavCredentials] or loaded from storage.
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
