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
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.ui.navigation.SharedViewModelStoreNavEntryDecorator
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

/**
 * Shares the loaded occurrence between the event detail screen and the event edit screen, so edit composes its first frame with
 * an already loaded event instead of going through [EventDetailUiState.Displayable.Loading] again and having nothing to animate
 * towards.
 *
 * To do so, we explicitly share the instance of the view model between the two screens using a [SharedViewModelStoreNavEntryDecorator].
 */
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class EventDetailSharedDataViewModel(
    eventLoader: EventLoader,
) : ViewModel() {
    private val occurrenceIdFlow = MutableStateFlow<OccurrenceId?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val eventAndCalendar: StateFlow<EventAndCalendarState> = occurrenceIdFlow
        .filterNotNull()
        .flatMapLatest(eventLoader::eventAndCalendar)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), EventAndCalendarState.Loading)

    //region Event detail UI
    private val eventDetailUiState: StateFlow<EventDetailUiState> = eventLoader
        .eventDetailUi(eventAndCalendar)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), EventDetailUiState.Displayable.Loading)

    val eventDetailUi: StateFlow<EventDetailUiState.Displayable> = eventDetailUiState
        .filterIsInstance<EventDetailUiState.Displayable>()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), EventDetailUiState.Displayable.Loading)

    val goBackRequests: ReceiveChannel<EventDetailUiState.Unavailable> = eventDetailUiState
        .filterIsInstance<EventDetailUiState.Unavailable>()
        .produceIn(viewModelScope)
    //endregion

    fun setOccurrenceId(occurrenceId: OccurrenceId) {
        occurrenceIdFlow.value = occurrenceId
    }
}
