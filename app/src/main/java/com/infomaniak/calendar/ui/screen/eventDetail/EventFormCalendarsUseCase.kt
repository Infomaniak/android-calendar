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
package com.infomaniak.calendar.ui.screen.eventDetail

import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.manager.CachedCalendarManager
import com.infomaniak.calendar.ui.screen.eventDetail.model.EventFormCalendars
import com.infomaniak.calendar.utils.combineStates
import com.infomaniak.calendar.utils.mapState
import com.infomaniak.calendar.utils.toEventDetailCalendar
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.Calendar
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlin.time.Duration.Companion.seconds

/**
 * The calendars an event can be created in or moved to, shared by the creation, the detail and the edition screens.
 *
 * Read-only calendars are filtered out: the user cannot write an event in them, so they must not be offered by the form.
 */
class EventFormCalendarsUseCase @Inject constructor(private val cachedCalendarManager: CachedCalendarManager) {
    private val useCaseScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val writableCalendars: StateFlow<List<EventDetailCalendar>> = cachedCalendarManager.writableCalendars
        .mapState(useCaseScope, SharingStarted.WhileSubscribed(5.seconds)) { it.map(Calendar::toEventDetailCalendar) }

    /**
     * All writable calendars, preselecting the one the user last created an event in.
     *
     * Only null if the user has no writable calendar at all.
     */
    val creationCalendars: StateFlow<EventFormCalendars?> = combineStates(
        flow1 = writableCalendars,
        flow2 = cachedCalendarManager.lastUsedCalendarId,
        scope = useCaseScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
    ) { calendars, lastUsedCalendarId ->
        val initialCalendar = calendars.firstOrNull { it.id == lastUsedCalendarId } ?: calendars.firstOrNull()
        initialCalendar?.let { EventFormCalendars(calendars, it) }
    }
}
