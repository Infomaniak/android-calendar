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
package com.infomaniak.calendar.manager

import com.infomaniak.calendar.data.SettingsDataValues
import com.infomaniak.calendar.model.settings.SettingTimeZone
import com.infomaniak.calendar.model.settings.toTimeZone
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone

/** The time zone the whole app shows dates in, following the time zone setting. */
@Inject
@SingleIn(AppScope::class)
class TimeZoneManager(settingsDataValues: SettingsDataValues) {
    val timeZone: Flow<TimeZone> = settingsDataValues.timeZone.flow.map(SettingTimeZone::toTimeZone).distinctUntilChanged()
}
