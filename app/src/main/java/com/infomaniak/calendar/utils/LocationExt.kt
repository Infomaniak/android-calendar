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

import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.multiplatform_calendar.bookable_resources.domain.model.BookableResourceUuid
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Location

fun Location.toLocationUi(): EventDetailUi.LocationUi? = when (this) {
    is Location.Place -> toPlaceUi()
    is Location.BookableRoom -> resource?.let {
        EventDetailUi.LocationUi.Room(id = it.uuid.value, title = it.name, seats = it.capacity, floor = it.floor, location = it.location)
    }
}

/** [current] is only read for a room unknown locally: the form can't show it, so saving would otherwise drop its booking. */
fun EventDetailUi.LocationUi?.toLocation(current: Location?): Location? = when (this) {
    is EventDetailUi.LocationUi.Place -> Location.Place(toLocationText())
    is EventDetailUi.LocationUi.Room -> Location.BookableRoom(BookableResourceUuid(id), resource = null)
    null -> current.takeIf { it is Location.BookableRoom && it.resource == null }
}

// TODO: Wait for the KMP decision on how a place's name and address are stored, it only holds the `LOCATION` text for now
private fun Location.Place.toPlaceUi(): EventDetailUi.LocationUi.Place {
    val lines = text.lines()
    return EventDetailUi.LocationUi.Place(name = lines.first(), address = lines.drop(1).joinToString("\n").ifBlank { null })
}

// TODO: Wait for the KMP decision on how a place's name and address are stored, it only holds the `LOCATION` text for now
private fun EventDetailUi.LocationUi.Place.toLocationText(): String = listOfNotNull(name, address).joinToString("\n")
