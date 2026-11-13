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
package com.infomaniak.calendar.components.foundation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** An event's location: a place, or a bookable room. */
sealed interface LocationUi : Parcelable {
    /** What a maps app searches to find this location. */
    val mapsQuery: String?
        get() = when (this) {
            is Place -> address ?: name
            is Room -> address
        }

    /** @param address Under the place's [name], null when the name already is its address. */
    @Parcelize
    data class Place(val name: String, val address: String?) : LocationUi

    /**
     * @param id The bookable resource's id.
     * @param address Where the room is, e.g. its building's address.
     * @param availability Over the event's time, only known in the location search.
     */
    @Parcelize
    data class Room(
        val id: String,
        val name: String,
        val seats: Int?,
        val floor: Int?,
        val address: String?,
        val availability: RoomAvailability?,
    ) : LocationUi
}
