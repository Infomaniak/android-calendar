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
package com.infomaniak.calendar.components.foundation.manager

import coil3.ImageLoader
import com.infomaniak.calendar.components.foundation.models.AvatarData

interface AvatarFinder {
    suspend fun avatarOf(name: String?, email: String): AvatarData?

    /** The loader fetching avatars of [accountId] (`null` for device avatars); `null` shows initials instead. */
    fun imageLoaderOf(accountId: Long?): ImageLoader?

    companion object {
        val Empty = object : AvatarFinder {
            override suspend fun avatarOf(name: String?, email: String): AvatarData? = null
            override fun imageLoaderOf(accountId: Long?): ImageLoader? = null
        }
    }
}
