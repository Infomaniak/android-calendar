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

import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.manager.CachedCalendarManager
import com.infomaniak.calendar.ui.screen.eventDetail.model.EventFormCalendars
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.calendar.utils.toEventDetailCalendar
import com.infomaniak.calendar.utils.toEventDetailCalendarGroups
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

/**
 * The calendars an event can be created in.
 *
 * Read-only calendars are filtered out: the user cannot write an event in them, so they must not be offered by the form.
 * The calendars an existing event can be defined in are carried by its [EventDetailUi].
 */
class EventFormCalendarsUseCase @Inject constructor(
    accountUtils: AccountUtils,
    cachedCalendarManager: CachedCalendarManager,
) {
    private val useCaseScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * All writable calendars grouped by account, preselecting the one the user last created an event in.
     *
     * Null while the users load, or if the user has no writable calendar at all.
     */
    val creationCalendars: StateFlow<EventFormCalendars?> = combine(
        cachedCalendarManager.writableCalendars,
        cachedCalendarManager.lastUsedCalendarId,
        accountUtils.users,
    ) { calendars, lastUsedCalendarId, users ->
        val initialCalendar = calendars.firstOrNull { it.id.url == lastUsedCalendarId } ?: calendars.firstOrNull()
        initialCalendar?.let { EventFormCalendars(users.toEventDetailCalendarGroups(calendars), it.toEventDetailCalendar()) }
    }.stateIn(useCaseScope, SharingStarted.WhileSubscribed(5.seconds), initialValue = null)
}
