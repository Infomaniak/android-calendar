package com.infomaniak.calendar.secured

import com.infomaniak.core.auth.models.user.User
import com.infomaniak.multiplatform_calendar.core.domain.model.account.DavCredentials
import com.infomaniak.multiplatform_calendar.core.managers.AccountManager
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

/**
 * Repository responsible for the secure persistence of DAV credentials, keyed by user ID.
 *
 * Credentials are encrypted via [KeystoreCipher] (AES/GCM, key stored in the Android Keystore)
 * before being saved to [CalendarDataValues] as [SecuredDavCredential] entries.
 * DAV credentials are fetched from the server through [AccountManager] during [save].
 */
class SecuredDavCredentialsRepository @Inject constructor(
    private val keystoreCipher: KeystoreCipher,
    private val dataValues: CalendarDataValues,
    private val accountManager: AccountManager,
) {
    /**
     * Retrieves and decrypts the DAV credentials for the given user.
     *
     * @param userId Unique identifier of the user.
     * @return The decrypted [DavCredentials].
     */
    suspend fun get(userId: Long): DavCredentials {
        val secured = dataValues.securedDavCredential.flow.first()[userId]!!
        return DavCredentials(
            username = keystoreCipher.decrypt(secured.encryptedUsername, secured.usernameIV),
            password = keystoreCipher.decrypt(secured.encryptedPassword, secured.passwordIV),
        )
    }

    /**
     * Fetches, encrypts, and persists the DAV credentials for the given user.
     *
     * DAV credentials are retrieved from the server via [AccountManager] using the user's
     * access token and login. The username and password are then encrypted separately,
     * each with its own IV. Any existing credentials for this user are replaced.
     *
     * @param user The authenticated user whose DAV credentials should be fetched and stored.
     */
    suspend fun save(user: User) {
        val davCredential = accountManager.retrieveDavCredential(authToken = user.apiToken.accessToken, login = user.login)

        val encryptedUsername: EncryptionResult = keystoreCipher.encrypt(davCredential.username)
        val encryptedPassword: EncryptionResult = keystoreCipher.encrypt(davCredential.password)

        dataValues.securedDavCredential.update { current ->
            val securedDavCredential = SecuredDavCredential(
                encryptedUsername = encryptedUsername.encryptedData,
                usernameIV = encryptedUsername.initializationVector,
                encryptedPassword = encryptedPassword.encryptedData,
                passwordIV = encryptedPassword.initializationVector,
            )
            current + (user.id.toLong() to securedDavCredential)
        }
    }

    /**
     * Removes the DAV credentials for the given user from persistent storage.
     *
     * @param userId Unique identifier of the user.
     */
    suspend fun remove(userId: Long) {
        dataValues.securedDavCredential.update { it - userId }
    }
}
