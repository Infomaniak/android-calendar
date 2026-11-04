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
package com.infomaniak.calendar.components.foundation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.infomaniak.calendar.components.foundation.manager.AvatarFinder
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.AvatarData
import com.infomaniak.calendar.components.foundation.models.AvatarStrategy
import com.infomaniak.calendar.components.foundation.state.LocalAvatarFinder
import com.infomaniak.core.avatar.LocalAvatarColors
import com.infomaniak.core.avatar.getBackgroundColorResBasedOnId
import com.infomaniak.core.avatar.models.AvatarColors
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.avatar.models.AvatarUrlData

@Composable
fun AvatarType.Companion.fromAttendee(attendee: AttendeeUi): State<AvatarType> {
    val avatarColors = LocalAvatarColors.current
    val avatarFinder = LocalAvatarFinder.current
    val avatar = attendee.avatar

    fun getUrlOrInitials(avatarUrlData: AvatarUrlData?): AvatarType.WithInitials = AvatarType.getUrlOrInitials(
        avatarUrlData = avatarUrlData,
        initials = attendee.initials(),
        colors = AvatarColors(
            containerColor = getBackgroundColorResBasedOnId(attendee.email.hashCode(), avatarColors.containerColors),
            contentColor = avatarColors.contentColor,
        ),
    )

    val initialValue = if (avatar is AvatarStrategy.Precomputed) {
        avatarFinder.avatarUrlDataOf(avatar.avatarData)
    } else {
        null
    }

    return produceState(getUrlOrInitials(initialValue)) {
        if (avatar is AvatarStrategy.ToCompute) {
            val avatarData = avatarFinder.avatarOf(attendee.displayName, attendee.email)
            value = getUrlOrInitials(avatarFinder.avatarUrlDataOf(avatarData))
        }
    }
}

fun AvatarFinder.avatarUrlDataOf(avatarData: AvatarData?): AvatarUrlData? {
    val data = avatarData ?: return null
    return imageLoaderOf(data.accountId)?.let { AvatarUrlData(url = data.url, imageLoader = it) }
}
