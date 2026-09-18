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

import androidx.lifecycle.ViewModel
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.calendar.utils.toEventDetailUi
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class EventDetailViewModel(
    accountUtils: AccountUtils,
    private val calendarManager: CalendarManager,
) : ViewModel() {
    private val occurrenceIdFlow: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val eventFlow = occurrenceIdFlow
        .distinctUntilChanged()
        .flatMapLatest { occurrenceId -> calendarManager.observeOccurrence(occurrenceId) }

    fun setOccurrenceId(occurrenceId: String) {
        occurrenceIdFlow.tryEmit(occurrenceId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventDetailUi = eventFlow
        .flatMapLatest { event ->
            if (event == null) {
                flowOf(null)
            } else {
                calendarManager
                    .observeCalendars()
                    .map { it.find { calendar -> calendar.id == event.calendarId } }
                    .map { calendar -> calendar?.let { event to it } }
            }
        }
        .combine(accountUtils.emailsByUserId) { eventAndCalendar, emailsByUserId ->
            val (event, calendar) = eventAndCalendar ?: return@combine EventDetailUiState.Deleted

            event
                .toEventDetailUi(calendar, emailsByUserId)
                .let(EventDetailUiState::Success)
        }
}

/**
 * TODO[occurrenceId]: [OccurrenceId.Recurrence] is internal to the KMP module, so a serialized occurrence id cannot be
 *  parsed back and every id is read as a master. Occurrences of a recurring series therefore still resolve to their
 *  master event.
 */
private fun CalendarManager.observeOccurrence(occurrenceId: String) = observeOccurrence(OccurrenceId.Master(EventId(occurrenceId)))
