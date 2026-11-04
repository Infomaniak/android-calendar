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

import com.infomaniak.calendar.ui.model.TimelineUiState
import com.infomaniak.calendar.utils.toDayEventsByDate
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

/** Observes the events the day, 3 days and week views show, sorted per day into all-day and timed events. */
@SingleIn(AppScope::class)
@Inject
class DayEventsLoader(visibleEventsManager: VisibleEventsManager) {
    private val useCaseScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    val observeEventsByDate: StateFlow<TimelineUiState> = visibleEventsManager.loadedEvents
        .filterNotNull()
        .mapLatest { loadedEvents ->
            TimelineUiState.Success(loadedEvents.slicesByDay.toDayEventsByDate(loadedEvents.timeZone))
        }
        .stateIn(
            scope = useCaseScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = TimelineUiState.Loading,
        )
}
