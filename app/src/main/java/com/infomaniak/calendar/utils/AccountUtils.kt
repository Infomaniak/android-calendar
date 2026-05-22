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
package com.infomaniak.calendar.utils

import com.infomaniak.core.auth.UserExistenceChecker
import com.infomaniak.core.auth.models.user.User
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Lightweight account management entry point used by the onboarding flow.
 *
 * The Calendar app does not yet persist users, so this is intentionally a stub:
 *  - [isUserAlreadyPresent] always returns `false` so a freshly logged-in user is never rejected
 *    as a duplicate;
 *  - [addUser] is a TODO that will plug into the real user persistence layer once it lands.
 *
 * The shape of this class mirrors `com.infomaniak.swisstransfer.ui.utils.AccountUtils` so the
 * persistence layer can be slotted in without touching the onboarding code.
 */
@Inject
@SingleIn(AppScope::class)
class AccountUtils : UserExistenceChecker {

    override suspend fun isUserAlreadyPresent(userId: Int): Boolean {
        // TODO: Check the local user store once it exists.
        return false
    }

    /**
     * Persist a freshly authenticated user.
     *
     * TODO: Wire this up to the real user persistence layer (DB / DataStore / KMP user manager).
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun addUser(user: User) {
        // No-op stub; see class kdoc.
    }
}
