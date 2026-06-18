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

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val DAV_CREDENTIAL_KEY = "davCredentialKey"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_LENGTH = 128
private const val AES_KEY_SIZE = 256

class KeystoreCipher @Inject constructor() {
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private val secretKey: SecretKey
        @Synchronized get() = (keyStore.getEntry(DAV_CREDENTIAL_KEY, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: generateNewKey()

    private fun generateNewKey(): SecretKey {
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(DAV_CREDENTIAL_KEY, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(AES_KEY_SIZE)
                    .build(),
            )
        }.generateKey()
    }

    suspend fun encrypt(password: String): Pair<String, String> = withContext(Dispatchers.IO) {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val initializationVector = cipher.iv.encodeToBase64()
        val encryptedData = cipher.doFinal(password.toByteArray(Charsets.UTF_8)).encodeToBase64()
        initializationVector to encryptedData
    }

    suspend fun decrypt(encryptedPasswordBase64: String, initializationVectorBase64: String): String {
        return withContext(Dispatchers.IO) {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, initializationVectorBase64.decodeFromBase64())
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            String(cipher.doFinal(encryptedPasswordBase64.decodeFromBase64()), Charsets.UTF_8)
        }
    }

    private fun ByteArray.encodeToBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)
    private fun String.decodeFromBase64(): ByteArray = Base64.decode(this, Base64.NO_WRAP)
}
