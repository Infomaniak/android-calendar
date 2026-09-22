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

import com.infomaniak.calendar.data.CalendarDataValues
import com.infomaniak.calendar.utils.mapState
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * App-wide, always warm calendars, so screens can read them synchronously and never go through an empty/loading state.
 *
 * Created in [com.infomaniak.calendar.MainApplication.onCreate] so the queries have long completed by the time a screen needs them.
 */
@SingleIn(AppScope::class)
class CachedCalendarManager @Inject constructor(calendarManager: CalendarManager, calendarDataValues: CalendarDataValues) {
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val calendars = calendarManager
        .observeCalendars()
        .stateIn(managerScope, SharingStarted.Eagerly, emptyList())

    val writableCalendars = calendars.mapState { calendars -> calendars.filter { it.accessLevel.canWrite } }

    val lastUsedCalendarId = calendarDataValues.lastUsedCalendarId.flow.stateIn(managerScope, SharingStarted.Eagerly, null)
}
