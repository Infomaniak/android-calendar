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

import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.manager.CachedCalendarManager
import com.infomaniak.calendar.ui.screen.eventDetail.scope.RecurrentEventScope
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.calendar.utils.account.accountId
import com.infomaniak.calendar.utils.applyTo
import com.infomaniak.calendar.utils.toNewEventEditData
import com.infomaniak.core.common.cancellable
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Organizer
import com.infomaniak.multiplatform_calendar.core.domain.model.event.recurrence.RecurrenceScope
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

/**
 * Single entry point for the screens that create, edit or delete events
 */
class EventComposerUseCase @Inject constructor(
    private val calendarManager: CalendarManager,
    private val cachedCalendarManager: CachedCalendarManager,
    private val accountUtils: AccountUtils,
) {
    suspend fun createEvent(eventDraft: EventDraft): SaveEventResult {
        return runCatching {
            val defaultOrganizer = accountUserOf(CalendarId(eventDraft.calendarId))
            calendarManager.createEvent(eventDraft.toNewEventEditData(defaultOrganizer))
            SaveEventResult.Success
        }.cancellable().getOrDefault(SaveEventResult.Error)
    }

    suspend fun updateEvent(occurrenceId: OccurrenceId, scope: RecurrentEventScope, eventDraft: EventDraft): SaveEventResult {
        return runCatching {
            val event = calendarManager.getOccurrence(occurrenceId) ?: return SaveEventResult.Error
            val editData = calendarManager.getEditData(occurrenceId) ?: return SaveEventResult.Error
            calendarManager.updateEvent(
                target = event.targetFor(scope.toRecurrenceScope()),
                data = eventDraft.applyTo(editData, defaultOrganizer = accountUserOf(editData.calendarId)),
            )
            SaveEventResult.Success
        }.cancellable().getOrDefault(SaveEventResult.Error)
    }

    suspend fun deleteEvent(occurrenceId: OccurrenceId, scope: RecurrentEventScope): DeleteEventResult {
        return runCatching {
            val event = calendarManager.getOccurrence(occurrenceId) ?: return DeleteEventResult.Error
            calendarManager.deleteEvent(event.targetFor(scope.toRecurrenceScope()))
            DeleteEventResult.Success
        }.cancellable().getOrDefault(DeleteEventResult.Error)
    }

    private suspend fun accountUserOf(calendarId: CalendarId): Organizer? {
        val accountId = cachedCalendarManager.calendars.value.find { it.id == calendarId }?.accountId ?: return null
        val user = accountUtils.users.first().find { it.accountId == accountId } ?: return null
        return Organizer(email = user.email, displayName = user.displayName)
    }
}

sealed interface SaveEventResult {
    object Success : SaveEventResult
    object Error : SaveEventResult
}

sealed interface DeleteEventResult {
    object Success : DeleteEventResult
    object Error : DeleteEventResult
}

private fun RecurrentEventScope.toRecurrenceScope(): RecurrenceScope = when (this) {
    RecurrentEventScope.ThisEvent -> RecurrenceScope.ThisOccurrence
    RecurrentEventScope.ThisAndFollowingEvents -> RecurrenceScope.ThisAndFollowing
    RecurrentEventScope.AllEvents -> RecurrenceScope.AllOccurrences
}
