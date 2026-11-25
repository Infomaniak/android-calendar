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
import androidx.compose.runtime.remember
import androidx.compose.ui.res.pluralStringResource
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatFullDateWithYear
import com.infomaniak.calendar.components.resources.R
import kotlinx.datetime.LocalDate

/** How a day of a calendar grid is announced: its full date, then its [eventCount] when it has any. */
@Composable
fun dayContentDescription(date: LocalDate, eventCount: Int): String {
    val fullDate = date.formatFullDateWithYear()
    val events = if (eventCount == 0) {
        null
    } else {
        pluralStringResource(R.plurals.contentDescriptionEventCount, eventCount, eventCount)
    }
    return remember(fullDate, events) { listOfNotNull(fullDate, events).joinToString() }
}
