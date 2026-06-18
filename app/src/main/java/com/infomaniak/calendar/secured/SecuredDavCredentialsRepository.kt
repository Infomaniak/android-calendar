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

import com.infomaniak.multiplatform_calendar.core.domain.model.account.DavCredentials
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

class SecuredDavCredentialsRepository @Inject constructor(
    private val keystoreCipher: KeystoreCipher,
    private val dataValues: CalendarDataValues,
) {
    suspend fun get(userId: Long): DavCredentials? {
        val secured = dataValues.securedDavCredential.flow.first()[userId] ?: return null
        return runCatching {
            DavCredentials(
                username = keystoreCipher.decrypt(secured.encryptedUsernameBase64, secured.usernameIvBase64),
                password = keystoreCipher.decrypt(secured.encryptedPasswordBase64, secured.initializationVectorBase64),
            )
        }.getOrNull()
    }

    suspend fun save(userId: Long, credential: DavCredentials) {
        val (usernameIv, encryptedUsername) = keystoreCipher.encrypt(credential.username)
        val (passwordIv, encryptedPassword) = keystoreCipher.encrypt(credential.password)
        dataValues.securedDavCredential.update { current ->
            current + (userId to SecuredDavCredential(encryptedUsername, usernameIv, encryptedPassword, passwordIv))
        }
    }

    suspend fun remove(userId: Long) {
        dataValues.securedDavCredential.update { it - userId }
    }
}
