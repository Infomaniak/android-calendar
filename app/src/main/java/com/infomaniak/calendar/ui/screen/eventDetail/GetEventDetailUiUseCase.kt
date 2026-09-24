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
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

/**
 * Observes the [EventDetailUiState] of the occurrence set through [setOccurrenceId].
 *
 * There is a single shared flow, and it is what makes the detail -> edit transition work: both screens display the same
 * occurrence, so [distinctUntilChanged] turns edit's [setOccurrenceId] into a no-op and [SharingStarted.WhileSubscribed] keeps
 * the state warm in between. Edit therefore composes its first frame with an already loaded event, instead of going through
 * [EventDetailUiState.Loading] again and having nothing to animate towards.
 */
@SingleIn(AppScope::class)
class GetEventDetailUiUseCase @Inject constructor(
    accountUtils: AccountUtils,
    private val calendarManager: CalendarManager,
) {
    private val useCaseScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val occurrenceIdFlow = MutableStateFlow<OccurrenceId?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventFlow = occurrenceIdFlow
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { occurrenceId -> calendarManager.observeOccurrence(occurrenceId) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventDetailUi: StateFlow<EventDetailUiState> = eventFlow
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
            val (event, calendar) = eventAndCalendar ?: return@combine EventDetailUiState.Unavailable

            event
                .toEventDetailUi(calendar, emailsByUserId)
                .let(EventDetailUiState::Success)
        }
        .stateIn(useCaseScope, SharingStarted.WhileSubscribed(5.seconds), EventDetailUiState.Loading)

    fun setOccurrenceId(occurrenceId: OccurrenceId) {
        occurrenceIdFlow.value = occurrenceId
    }
}
