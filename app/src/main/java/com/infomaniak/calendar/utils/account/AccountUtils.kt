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
package com.infomaniak.calendar.utils.account

import android.content.Context
import com.infomaniak.calendar.MainApplication
import com.infomaniak.calendar.secured.DavCredentialsManager
import com.infomaniak.core.auth.PersistedCurrentUserAccountUtils
import com.infomaniak.core.auth.models.user.User
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class AccountUtils(
    appContext: Context,
    private val davCredentialsManager: DavCredentialsManager,
) : PersistedCurrentUserAccountUtils(appContext, MainApplication.userDataCleanableList) {
    suspend fun addUser(calendarUser: CalendarUser) {
        val user = calendarUser.user
        davCredentialsManager.addCredential(user = user, davCredentials = calendarUser.davCredentials)
        super.addUser(user)
    }

    @Deprecated("Use addUser with a CalendarUser instead", replaceWith = ReplaceWith("addUser(calendarUser)"))
    override suspend fun addUser(user: User) {
        super.addUser(user)
    }

    override suspend fun removeUser(userId: Int) {
        davCredentialsManager.removeCredential(userId.toLong())
        super.removeUser(userId)
    }
}
