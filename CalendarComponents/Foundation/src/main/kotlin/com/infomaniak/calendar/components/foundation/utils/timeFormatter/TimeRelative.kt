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
package com.infomaniak.calendar.components.foundation.utils.timeFormatter

import android.text.format.DateUtils
import com.infomaniak.calendar.components.foundation.utils.CapitalizationUtils.capitalizeFirstLetter
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Formats this instant relative to now in a localized, human-readable way, e.g. "In 5 minutes", "In 25 hours" or
 * "5 minutes ago". Delegates to the platform's [DateUtils.getRelativeTimeSpanString] so the wording, pluralization and
 * locale all follow the system.
 */
fun Instant.formatRelativeToNow(): String = DateUtils.getRelativeTimeSpanString(
    toEpochMilliseconds(),
    Clock.System.now().toEpochMilliseconds(),
    DateUtils.MINUTE_IN_MILLIS,
).toString()
