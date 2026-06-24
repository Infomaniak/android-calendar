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

import kotlinx.serialization.Serializable

/**
 * Serializable representation of a user's DAV credentials stored in encrypted form.
 *
 * Each sensitive field (username and password) is encrypted independently using AES/GCM
 * and encoded as Base64. Each encryption produces its own ciphertext and a unique
 * initialization vector (IV), both required for decryption.
 *
 * @property encryptedUsername Encrypted username, Base64-encoded.
 * @property usernameIV Initialization vector used to encrypt the username, Base64-encoded.
 * @property encryptedPassword Encrypted password, Base64-encoded.
 * @property passwordIV Initialization vector used to encrypt the password, Base64-encoded.
 *
 * @see KeystoreCipher for the encryption/decryption logic.
 * @see SecuredDavCredentialsRepository for persistence and retrieval.
 */
@Serializable
data class SecuredDavCredentials(
    val encryptedUsername: String,
    val usernameIV: String,
    val encryptedPassword: String,
    val passwordIV: String,
)
