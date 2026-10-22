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

import com.infomaniak.calendar.ui.screen.eventDetail.delete.DeleteEventScope
import com.infomaniak.core.common.cancellable
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.recurrence.RecurrenceScope
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.Inject

/**
 * Single entry point for the screens that create, edit or delete events
 */
class EventComposerUseCase @Inject constructor(private val calendarManager: CalendarManager) {
    suspend fun deleteEvent(occurrenceId: OccurrenceId, deleteEventScope: DeleteEventScope): DeleteEventResult {
        return runCatching {
            val event = calendarManager.getOccurrence(occurrenceId) ?: return DeleteEventResult.Error
            calendarManager.deleteEvent(event.targetFor(deleteEventScope.toRecurrenceScope()))
            DeleteEventResult.Success
        }.cancellable().getOrDefault(DeleteEventResult.Error)
    }
}

sealed interface DeleteEventResult {
    object Success : DeleteEventResult
    object Error : DeleteEventResult
}

private fun DeleteEventScope.toRecurrenceScope(): RecurrenceScope = when (this) {
    DeleteEventScope.ThisEvent -> RecurrenceScope.ThisOccurrence
    DeleteEventScope.ThisAndFollowingEvents -> RecurrenceScope.ThisAndFollowing
    DeleteEventScope.AllEvents -> RecurrenceScope.AllOccurrences
}
