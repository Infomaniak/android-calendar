package com.infomaniak.calendar.secured

import com.infomaniak.multiplatform_calendar.core.domain.model.account.DavCredentials
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

/**
 * Repository responsible for the secure persistence of DAV credentials, keyed by user ID.
 *
 * Credentials are encrypted via [KeystoreCipher] (AES/GCM, key stored in the Android Keystore)
 * before being saved to [CalendarDataValues] as [SecuredDavCredential] entries.
 */
class SecuredDavCredentialsRepository @Inject constructor(
    private val keystoreCipher: KeystoreCipher,
    private val dataValues: CalendarDataValues,
) {

    /**
     * Retrieves and decrypts the DAV credentials for the given user.
     *
     * @param userId Unique identifier of the user.
     * @return The decrypted [DavCredentials], or `null` if no credentials are found
     * or if decryption fails (corrupted key, tampered data, etc.).
     */
    suspend fun get(userId: Long): DavCredentials? {
        val secured = dataValues.securedDavCredential.flow.first()[userId] ?: return null
        return runCatching {
            DavCredentials(
                username = keystoreCipher.decrypt(secured.encryptedUsername, secured.usernameIV),
                password = keystoreCipher.decrypt(secured.encryptedPassword, secured.passwordIV),
            )
        }.getOrNull()
    }

    /**
     * Encrypts and persists the DAV credentials for the given user.
     *
     * Any existing credentials for [userId] are replaced.
     * The username and password are encrypted separately, each with its own IV.
     *
     * @param userId Unique identifier of the user.
     * @param credential Plain-text credentials to encrypt and store.
     */
    suspend fun save(userId: Long, credential: DavCredentials) {
        val encryptedUsername: EncryptionResult = keystoreCipher.encrypt(credential.username)
        val encryptedPassword: EncryptionResult = keystoreCipher.encrypt(credential.password)

        dataValues.securedDavCredential.update { current ->
            val securedDavCredential = SecuredDavCredential(
                encryptedUsername = encryptedUsername.encryptedData,
                usernameIV = encryptedUsername.initializationVector,
                encryptedPassword = encryptedPassword.encryptedData,
                passwordIV = encryptedPassword.initializationVector,
            )
            current + (userId to securedDavCredential)
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
