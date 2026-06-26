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

import com.infomaniak.core.datavalue.DataValueSerializer
import com.infomaniak.multiplatform_calendar.core.domain.model.account.DavCredentials
import kotlinx.serialization.json.Json

class EncryptedDavCredentialSerializer(private val keystoreCipher: KeystoreCipher) : DataValueSerializer<Map<Long, DavCredentials>> {
    override suspend fun serialize(value: Map<Long, DavCredentials>): String {
        val secured = value.mapValues { (_, davCredentials) ->
            val encryptedUsername = keystoreCipher.encrypt(davCredentials.username)
            val encryptedPassword = keystoreCipher.encrypt(davCredentials.password)
            SecuredDavCredentials(
                encryptedUsername = encryptedUsername.encryptedData,
                usernameIv = encryptedUsername.initializationVector,
                encryptedPassword = encryptedPassword.encryptedData,
                passwordIv = encryptedPassword.initializationVector,
            )
        }

        return Json.encodeToString(secured)
    }

    override suspend fun deserialize(value: String): Map<Long, DavCredentials> {
        return Json.decodeFromString<Map<Long, SecuredDavCredentials>>(value)
            .mapValues { (_, secured) ->
                DavCredentials(
                    username = keystoreCipher.decrypt(secured.encryptedUsername, secured.usernameIv),
                    password = keystoreCipher.decrypt(secured.encryptedPassword, secured.passwordIv),
                )
            }
    }
}
