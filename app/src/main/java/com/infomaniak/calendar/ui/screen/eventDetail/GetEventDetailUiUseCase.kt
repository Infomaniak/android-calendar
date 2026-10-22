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

import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.calendar.utils.toEventDetailUi
import com.infomaniak.core.sentry.SentryLog
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.Calendar
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Event
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetEventDetailUiUseCase @Inject constructor(
    private val accountUtils: AccountUtils,
    private val calendarManager: CalendarManager,
) {
    fun eventDetailUi(eventAndCalendar: Flow<EventAndCalendarState>): Flow<EventDetailUiState> = combine(
        eventAndCalendar,
        accountUtils.emailsByUserId,
    ) { eventAndCalendarState, emailsByUserId ->
        val (event, calendar) = when (eventAndCalendarState) {
            EventAndCalendarState.EventNotFound -> return@combine EventDetailUiState.Unavailable
            EventAndCalendarState.Loading -> return@combine EventDetailUiState.Displayable.Loading
            is EventAndCalendarState.Success -> eventAndCalendarState
        }

        event
            .toEventDetailUi(calendar, emailsByUserId)
            .let(EventDetailUiState.Displayable::Success)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun eventAndCalendar(occurrenceId: OccurrenceId): Flow<EventAndCalendarState> = calendarManager
        .observeOccurrence(occurrenceId)
        .flatMapLatest { event ->
            if (event == null) {
                flowOf(EventAndCalendarState.EventNotFound)
            } else {
                calendarManager
                    .observeCalendar(event.calendarId)
                    .map { calendar ->
                        if (calendar == null) {
                            val eventDetail = "masterEventId=${event.masterEventId}" +
                                    ", occurrenceId=${event.occurrenceId}" +
                                    ", calendarId=${event.calendarId}" +
                                    ", accountId=${event.accountId}"
                            SentryLog.w(TAG, "Event with no calendar for $eventDetail")
                            EventAndCalendarState.Loading
                        } else {
                            EventAndCalendarState.Success(event, calendar)
                        }
                    }
            }
        }

    companion object {
        private const val TAG = "GetEventDetailUiUseCase"
    }
}

sealed interface EventAndCalendarState {
    object Loading : EventAndCalendarState
    data class Success(val event: Event, val calendar: Calendar) : EventAndCalendarState
    object EventNotFound : EventAndCalendarState
}
