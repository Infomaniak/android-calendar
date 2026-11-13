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
package com.infomaniak.calendar.components.locationsearch.models

import androidx.compose.runtime.Immutable

/** A row of the location search: a place, typed or found, or a bookable room. */
sealed interface LocationUi {
    /** @param address Under the place's [name], null when the name already is its address. */
    @Immutable
    data class Place(val name: String, val address: String?) : LocationUi

    @Immutable
    data class Room(
        val id: String,
        val name: String,
        val seats: Int?,
        val floor: Int?,
        val availability: RoomAvailability,
    ) : LocationUi
}
