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

/**
 * Result of an AES/GCM encryption operation.
 *
 * @property initializationVector IV (initialization vector) used during encryption, Base64-encoded.
 * Must be stored alongside the ciphertext and supplied to [KeystoreCipher.decrypt].
 * @property encryptedData Encrypted payload, Base64-encoded.
 */
data class EncryptionResult(val initializationVector: String, val encryptedData: String)
