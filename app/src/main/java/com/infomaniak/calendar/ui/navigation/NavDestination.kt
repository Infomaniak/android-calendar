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
package com.infomaniak.calendar.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavDestination : NavKey {
    @Serializable
    data class Onboarding(val onlyLogin: Boolean = false) : NavDestination

    sealed interface CalendarView : NavDestination {

        /**
         * Stable identifier used to persist the last selected calendar view.
         *
         * It is written to disk, so it must never change, even if the destination is renamed.
         */
        val storageKey: String

        @Serializable
        data object Planning : CalendarView {
            override val storageKey get() = "planning"
        }

        @Serializable
        data object Day : CalendarView {
            override val storageKey get() = "day"
        }

        @Serializable
        data object ThreeDays : CalendarView {
            override val storageKey get() = "threeDays"
        }

        @Serializable
        data object Week : CalendarView {
            override val storageKey get() = "week"
        }

        @Serializable
        data object Month : CalendarView {
            override val storageKey get() = "month"
        }

        companion object {
            val Default: CalendarView = Planning

            private val byStorageKey by lazy {
                listOf(Planning, Day, ThreeDays, Week, Month).associateBy { it.storageKey }
            }

            /** Falls back to [Default] for unknown keys, so a removed or renamed view can never break the app launch. */
            fun fromStorageKey(storageKey: String): CalendarView = byStorageKey[storageKey] ?: Default
        }
    }

    sealed interface Accounts : NavDestination {
        @Serializable
        data object List : Accounts

        @Serializable
        data class Actions(val userId: Int) : Accounts
    }

    @Serializable
    data object EventCreation : NavDestination
}
