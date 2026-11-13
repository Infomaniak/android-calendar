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
package com.infomaniak.calendar.ui.screen.eventDetail.locationSearch

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.locationsearch.models.SearchRoomUi
import com.infomaniak.core.sentry.SentryLog
import com.infomaniak.multiplatform_calendar.bookable_resources.domain.model.BookableResource
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarBookableResourcesManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The rooms of the event form's location search, shared through the form's ViewModelStore: the form loads them as soon as
 * it opens, so they are usually ready once the location search opens.
 */
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class LocationSearchViewModel(
    private val calendarBookableResourcesManager: CalendarBookableResourcesManager,
) : ViewModel() {
    private val calendarId = MutableStateFlow<CalendarId?>(null)
    private var resources = emptyMap<String, BookableResource>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val rooms: StateFlow<List<SearchRoomUi>> = calendarId
        .filterNotNull()
        .flatMapLatest(::resourcesOf)
        .map { resources ->
            this.resources = resources.associateBy { it.uuid.value }
            resources.map { it.toSearchRoomUi() }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Loads the rooms bookable in [formState]'s calendar, following it as long as it's called. */
    suspend fun followForm(formState: EventFormState): Unit = coroutineScope {
        launch { dropRoomsLeftBehind(formState) }
        snapshotFlow { formState.calendar.id }.collect { id -> calendarId.value = CalendarId(id) }
    }

    /** The form's room for a room listed in [rooms]. */
    fun formRoomOf(room: SearchRoomUi): EventDetailUi.LocationUi.Room = EventDetailUi.LocationUi.Room(
        id = room.id,
        title = room.name,
        seats = room.seats,
        floor = room.floor,
        location = resources[room.id]?.location,
    )

    /**
     * Clears [formState]'s room when the calendar it switches to can't book it. Rooms are compared by id: two accounts can
     * each have a room of the same name. A switch cancels the check of the previous one, which could drop a room the
     * calendar switched back to can book.
     */
    private suspend fun dropRoomsLeftBehind(formState: EventFormState) {
        snapshotFlow { CalendarId(formState.calendar.id) }
            .drop(1) // The form's own calendar, only switching away from it can leave its room behind
            .collectLatest { calendarId ->
                val room = formState.location as? EventDetailUi.LocationUi.Room ?: return@collectLatest

                val resources = calendarBookableResourcesManager.observeResources(calendarId).firstOrNull() ?: return@collectLatest
                if (resources.none { it.uuid.value == room.id }) formState.location = null
            }
    }

    /** [calendarId]'s bookable resources, none if they can't be observed so a later calendar can still load its own. */
    private fun resourcesOf(calendarId: CalendarId): Flow<List<BookableResource>> {
        return calendarBookableResourcesManager.observeResources(calendarId).catch { throwable ->
            SentryLog.w(TAG, "Failed to observe the bookable resources", throwable)
            emit(emptyList())
        }
    }

    private fun BookableResource.toSearchRoomUi() = SearchRoomUi(id = uuid.value, name = name, seats = capacity, floor = floor)

    companion object {
        private const val TAG = "LocationSearchViewModel"
    }
}
