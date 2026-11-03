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

import com.infomaniak.calendar.components.foundation.models.SearchableText
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatCityAndCountry
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatLongName
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatUtcOffset
import kotlinx.datetime.TimeZone
import java.util.Locale
import kotlin.time.Clock

internal data class TimeZoneUi private constructor(
    val timeZone: TimeZone,
    val name: SearchableText,
    val location: SearchableText,
    val utcOffset: SearchableText,
) {
    /** Describes [timeZone] as it is right now, in the user's locale. */
    constructor(timeZone: TimeZone) : this(
        timeZone = timeZone,
        name = SearchableText(timeZone.formatLongName(Locale.getDefault(), Clock.System.now())),
        location = SearchableText(timeZone.formatCityAndCountry(Locale.getDefault())),
        utcOffset = SearchableText(timeZone.formatUtcOffset(Locale.getDefault(), Clock.System.now())),
    )

    private val searchableTexts = listOf(name, location, utcOffset, SearchableText(timeZone.id))

    fun matches(query: String): Boolean = searchableTexts.any { it.find(query) != null }
}
