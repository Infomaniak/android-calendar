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

import android.icu.util.TimeZone.SystemTimeZoneType
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.RequiresApi
import kotlinx.datetime.TimeZone
import android.icu.util.TimeZone as IcuTimeZone

/** ICU's region for zones not tied to a place, such as `Etc/GMT+5`. */
private const val WORLD_REGION = "001"

/** One entry per real-world location, skipping ICU zones unknown to kotlinx-datetime. */
internal fun availableTimeZones(): List<TimeZoneUi> {
    val ids = if (SDK_INT >= 28) canonicalLocationIds() else canonicalLocationIdsBeforeApi28()
    val knownIds = TimeZone.availableZoneIds // Get a single Set instance for all reads inside `filter`
    return ids.filter { it in knownIds }.map { TimeZoneUi(TimeZone.of(it)) }
}

@RequiresApi(28)
private fun canonicalLocationIds(): Set<String> {
    return IcuTimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL_LOCATION, null, null)
}

private fun canonicalLocationIdsBeforeApi28(): List<String> {
    return IcuTimeZone.getAvailableIDs().filter { id ->
        val isCanonical = IcuTimeZone.getCanonicalID(id) == id
        isCanonical && IcuTimeZone.getRegion(id) != WORLD_REGION
    }
}
