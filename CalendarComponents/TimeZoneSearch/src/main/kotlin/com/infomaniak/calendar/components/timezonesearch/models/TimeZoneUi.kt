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
package com.infomaniak.calendar.components.timezonesearch.models

import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatCityAndCountry
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatLongName
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatUtcOffset
import kotlinx.datetime.TimeZone
import java.util.Locale
import kotlin.time.Clock

internal data class TimeZoneUi private constructor(
    val timeZone: TimeZone,
    val name: String,
    val location: String,
    val utcOffset: String,
) {
    /** Describes [timeZone] as it is right now, in the user's locale. */
    constructor(timeZone: TimeZone) : this(
        timeZone = timeZone,
        name = timeZone.formatLongName(Locale.getDefault(), Clock.System.now()),
        location = timeZone.formatCityAndCountry(Locale.getDefault()),
        utcOffset = timeZone.formatUtcOffset(Locale.getDefault(), Clock.System.now()),
    )

    fun matches(query: String): Boolean = listOf(name, location, utcOffset, timeZone.id).any {
        it.contains(query, ignoreCase = true)
    }
}
