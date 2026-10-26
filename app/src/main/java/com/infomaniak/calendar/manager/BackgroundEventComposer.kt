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

import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.ui.screen.eventDetail.DeleteEventResult
import com.infomaniak.calendar.ui.screen.eventDetail.EventComposerUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.SaveEventResult
import com.infomaniak.calendar.ui.screen.eventDetail.scope.RecurrentEventScope
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Runs [EventComposerUseCase]'s changes in the background, one at a time, so the event screens close right away.
 * Temporary, until KMP is offline first.
 */
@SingleIn(AppScope::class)
class BackgroundEventComposer @Inject constructor(private val eventComposerUseCase: EventComposerUseCase) {
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val changeMutex = Mutex()

    private val _savingError = Channel<Unit>(Channel.CONFLATED)
    val savingError: ReceiveChannel<Unit> = _savingError

    fun createEvent(eventDraft: EventDraft) {
        enqueue { eventComposerUseCase.createEvent(eventDraft) == SaveEventResult.Success }
    }

    fun updateEvent(occurrenceId: OccurrenceId, scope: RecurrentEventScope, eventDraft: EventDraft) {
        enqueue { eventComposerUseCase.updateEvent(occurrenceId, scope, eventDraft) == SaveEventResult.Success }
    }

    fun deleteEvent(occurrenceId: OccurrenceId, scope: RecurrentEventScope) {
        enqueue { eventComposerUseCase.deleteEvent(occurrenceId, scope) == DeleteEventResult.Success }
    }

    private fun enqueue(change: suspend () -> Boolean) {
        managerScope.launch {
            val isSuccess = changeMutex.withLock { change() }
            if (!isSuccess) _savingError.trySend(Unit)
        }
    }
}
