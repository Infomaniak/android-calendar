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

import android.icu.text.TimeZoneNames
import androidx.compose.runtime.Composable
import kotlinx.datetime.TimeZone
import java.util.Locale

//region Exposed formatting methods
/**
 * `Paris`. Translates `Europe/Paris` -> `Paris` in the given locale.
 *
 * [TimeZoneNames.getExemplarLocationName] returns null if the time zone is not recognized by ICU. As a fallback, the last part of
 * the time zone ID is returned, e.g. `Europe/Paris` -> `Paris`. If the time zone ID is empty, null is returned.
 */
@Composable
fun TimeZone.formatCityName(locale: Locale = currentLocale()): String = formatLocalizedCityName(locale)
//endregion

//region Underlying testable logic
internal fun TimeZone.formatLocalizedCityName(locale: Locale): String {
    return TimeZoneNames.getInstance(locale).getExemplarLocationName(id) ?: id.substringAfterLast("/")
}
//endregion
