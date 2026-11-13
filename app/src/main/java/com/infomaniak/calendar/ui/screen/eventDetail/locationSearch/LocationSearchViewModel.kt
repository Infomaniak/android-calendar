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
import com.infomaniak.calendar.components.foundation.models.SearchableText
import com.infomaniak.calendar.components.locationsearch.models.LocationSearchResultUi
import com.infomaniak.calendar.components.locationsearch.models.RoomAvailability
import com.infomaniak.calendar.components.locationsearch.models.SearchPlaceUi
import com.infomaniak.calendar.components.locationsearch.models.SearchRoomUi
import com.infomaniak.calendar.manager.PlaceSearcher
import com.infomaniak.core.common.cancellable
import com.infomaniak.multiplatform_calendar.bookable_resources.domain.model.BookableResource
import com.infomaniak.multiplatform_calendar.bookable_resources.domain.model.BookableResourceUuid
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventTiming
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import com.infomaniak.multiplatform_calendar.core.managers.CalendarBookableResourcesManager
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
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
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * The results of the event form's location search, shared through the form's ViewModelStore: the form loads the rooms as
 * soon as it opens, so they are usually ready once the location search opens.
 */
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class LocationSearchViewModel(
    private val calendarBookableResourcesManager: CalendarBookableResourcesManager,
    private val calendarManager: CalendarManager,
    private val placeSearcher: PlaceSearcher,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val availabilityRequest = MutableStateFlow<RoomAvailabilityRequest?>(null)
    private var resources = emptyMap<String, BookableResource>()
    private var editedTiming: Pair<OccurrenceId, EventTiming?>? = null

    /** Null until known: the rooms are listed meanwhile with an unknown availability, then sorted once it is known. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val availability: Flow<Map<BookableResourceUuid, Boolean>?> = availabilityRequest.transformLatest { request ->
        emit(null)
        if (request != null) emit(availabilityOf(request))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rooms: StateFlow<List<SearchRoomUi>> = availabilityRequest
        .filterNotNull()
        .map { it.calendarId }
        .distinctUntilChanged()
        .flatMapLatest(calendarBookableResourcesManager::observeResources)
        .combine(availability) { resources, availability ->
            this.resources = resources.associateBy { it.uuid.value }
            resources.map { it.toSearchRoomUi(availability?.get(it.uuid)) }.sortedBy { it.availability.rank }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val places: Flow<List<SearchPlaceUi>> = query.mapLatest { query ->
        if (query.isEmpty()) emptyList() else placeSearcher.search(query)
    }

    private val searchableRooms: Flow<List<Pair<SearchRoomUi, SearchableText>>> = rooms.map { rooms ->
        rooms.map { it to SearchableText(it.name) }
    }

    /** The typed location first so it can be picked like a found place, then the found places, then the matching rooms. */
    val results: StateFlow<List<LocationSearchResultUi>> = combine(query, places, searchableRooms) { query, places, rooms ->
        val typedLocation = query.trim()
        val typedPlaces = if (typedLocation.isEmpty()) places else listOf(SearchPlaceUi(typedLocation, address = null)) + places
        val matchingRooms = rooms.mapNotNull { (room, name) ->
            room.takeIf { typedLocation.isEmpty() || name.find(typedLocation) != null }
        }
        typedPlaces.distinct() + matchingRooms
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), emptyList())

    fun search(query: String) {
        this.query.value = query
    }

    /**
     * Loads the rooms bookable in [formState]'s calendar with their availability over its time, following it as long as it's
     * called. [editedOccurrenceId] makes the rooms available only when free on every occurrence of its event.
     */
    suspend fun followForm(formState: EventFormState, editedOccurrenceId: OccurrenceId?): Unit = coroutineScope {
        launch { dropRoomsLeftBehind(formState) }
        snapshotFlow { formState.toRoomAvailabilityRequest(editedOccurrenceId) }.collect(availabilityRequest)
    }

    /** The form's room for a room listed in [results]. */
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

    private suspend fun availabilityOf(request: RoomAvailabilityRequest): Map<BookableResourceUuid, Boolean> {
        return runCatching {
            val editedTiming = request.editedOccurrenceId?.let { editedTimingOf(it) }
            val timing = editedTiming?.copy(bounds = request.bounds) ?: EventTiming(bounds = request.bounds)
            calendarBookableResourcesManager.availability(request.calendarId, timing)
        }.cancellable().getOrDefault(emptyMap())
    }

    /** Read once: only its recurrence is used, and reading an occurrence expands its whole series. */
    private suspend fun editedTimingOf(occurrenceId: OccurrenceId): EventTiming? {
        editedTiming?.let { (id, timing) -> if (id == occurrenceId) return timing }
        return calendarManager.getEditData(occurrenceId)?.timing.also { editedTiming = occurrenceId to it }
    }

    private fun BookableResource.toSearchRoomUi(isAvailable: Boolean?) = SearchRoomUi(
        id = uuid.value,
        name = name,
        seats = capacity,
        floor = floor,
        availability = when (isAvailable) {
            true -> RoomAvailability.Available
            false -> RoomAvailability.Unavailable
            null -> RoomAvailability.Unknown
        },
    )

    private val RoomAvailability.rank
        get() = when (this) {
            RoomAvailability.Available -> 0
            RoomAvailability.Unavailable -> 1
            RoomAvailability.Unknown -> 2
        }
}
