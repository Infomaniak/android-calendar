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
import com.infomaniak.calendar.ui.screen.eventDetail.model.EventCreationCalendars
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.calendar.utils.toEventDetailCalendar
import com.infomaniak.calendar.utils.toEventDetailCalendarGroups
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * The calendars an event can be created in.
 *
 * Read-only calendars are filtered out: the user cannot write an event in them, so they must not be offered by the form.
 * The calendars an existing event can be defined in are carried by its [EventDetailUi].
 */
class EventCreationCalendarsUseCase @Inject constructor(
    accountUtils: AccountUtils,
    cachedCalendarManager: CachedCalendarManager,
) {
    /**
     * All writable calendars grouped by account, preselecting the one the user last created an event in.
     *
     * Null if the user has no writable calendar at all.
     */
    val creationCalendars: Flow<EventCreationCalendars?> = combine(
        cachedCalendarManager.writableCalendars,
        cachedCalendarManager.lastUsedCalendarId,
        accountUtils.users,
    ) { calendars, lastUsedCalendarId, users ->
        val initialCalendar = calendars.firstOrNull { it.id.url == lastUsedCalendarId } ?: calendars.firstOrNull()
        initialCalendar?.let { EventCreationCalendars(users.toEventDetailCalendarGroups(calendars), it.toEventDetailCalendar()) }
    }
}
