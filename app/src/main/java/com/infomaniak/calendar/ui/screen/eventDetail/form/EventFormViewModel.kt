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
package com.infomaniak.calendar.ui.screen.eventDetail.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.form.EventFormValues
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.OrganizerSource
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.data.SettingsDataValues
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.screen.eventDetail.EventCreationCalendarsUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.screen.eventDetail.EventLoader
import com.infomaniak.calendar.utils.saveableAttendeesSearchState
import com.infomaniak.calendar.utils.saveableEventFormState
import com.infomaniak.calendar.utils.toEventFormValues
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

/**
 * The [EventFormViewModel] of [destination], from the store of [viewModelStoreOwner]: the form's own store for its screen,
 * [com.infomaniak.calendar.ui.navigation.LocalSharedViewModelStoreOwner] for the screens it opens.
 *
 * @param loadedEvent The edited event when already loaded, so the form is built right away instead of loading it again.
 */
@Composable
fun eventFormViewModel(
    destination: NavDestination.EventForm,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current),
    loadedEvent: EventDetailUi? = null,
): EventFormViewModel {
    val defaultExtras = (viewModelStoreOwner as? HasDefaultViewModelProviderFactory)?.defaultViewModelCreationExtras
    return viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        extras = MutableCreationExtras(defaultExtras ?: CreationExtras.Empty).apply {
            set(DestinationKey, destination)
            loadedEvent?.let { set(LoadedEventKey, it) }
        },
    )
}

private object DestinationKey : CreationExtras.Key<NavDestination.EventForm>
private object LoadedEventKey : CreationExtras.Key<EventDetailUi>

/**
 * Holds the state of an event form, shared with the screens it opens through its ViewModelStore.
 *
 * The state is built once its initial values are loaded, its fields restored from the [SavedStateHandle] when it holds them.
 */
@AssistedInject
class EventFormViewModel(
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted destination: NavDestination.EventForm,
    @Assisted loadedEvent: EventDetailUi?,
    private val eventLoader: EventLoader,
    private val eventCreationCalendarsUseCase: EventCreationCalendarsUseCase,
    private val settingsDataValues: SettingsDataValues,
) : ViewModel() {
    var state: EventFormState? by mutableStateOf(null)
        private set

    var attendeesSearchState: AttendeesSearchState<OrganizerSource>? by mutableStateOf(null)
        private set

    init {
        when (destination) {
            is NavDestination.EventCreation -> viewModelScope.launch {
                val attendeesSearchState = savedStateHandle.saveableAttendeesSearchState<OrganizerSource>()
                val formState = build(creationValues(destination), attendeesSearchState)
                snapshotFlow { formState.calendar }.collect(attendeesSearchState::followOrganizerOf)
            }
            is NavDestination.EventEdit -> if (loadedEvent != null) {
                buildEdit(loadedEvent)
            } else {
                viewModelScope.launch { buildEdit(loadEvent(destination)) }
            }
        }
    }

    private suspend fun creationValues(destination: NavDestination.EventCreation): EventFormValues = combine(
        eventCreationCalendarsUseCase.creationCalendars.filterNotNull(),
        settingsDataValues.defaultEventDuration.flow,
    ) { creationCalendars, defaultEventDuration ->
        EventFormValues.default(
            calendars = creationCalendars.calendars,
            calendar = creationCalendars.initialCalendar,
            date = destination.date,
            timeZone = TimeZone.currentSystemDefault(),
            eventDuration = defaultEventDuration,
        )
    }.first()

    private suspend fun loadEvent(destination: NavDestination.EventEdit): EventDetailUi {
        return eventLoader.eventDetailUi(eventLoader.eventAndCalendar(destination.occurrenceId))
            .filterIsInstance<EventDetailUiState.Displayable.Success>()
            .first()
            .eventDetail
    }

    private fun buildEdit(event: EventDetailUi) {
        val attendeesSearchState = savedStateHandle.saveableAttendeesSearchState<OrganizerSource>(
            initialParticipants = event.attendees.all,
            initialOrganizerSource = OrganizerSource.Static(event.organizer),
        )
        build(event.toEventFormValues(), attendeesSearchState)
    }

    private fun build(values: EventFormValues, attendeesSearchState: AttendeesSearchState<OrganizerSource>): EventFormState {
        this.attendeesSearchState = attendeesSearchState
        return savedStateHandle.saveableEventFormState(values, attendeesSearchState).also { state = it }
    }

    @AssistedFactory
    @ViewModelAssistedFactoryKey(EventFormViewModel::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): EventFormViewModel = create(
            savedStateHandle = extras.createSavedStateHandle(),
            destination = checkNotNull(extras[DestinationKey]) { "Use eventFormViewModel() to get an EventFormViewModel" },
            loadedEvent = extras[LoadedEventKey],
        )

        fun create(
            @Assisted savedStateHandle: SavedStateHandle,
            @Assisted destination: NavDestination.EventForm,
            @Assisted loadedEvent: EventDetailUi?,
        ): EventFormViewModel
    }
}

/** Organizing an event as the owner of its calendar, who must then not also be one of its attendees. */
private fun AttendeesSearchState<OrganizerSource>.followOrganizerOf(calendar: EventDetailCalendar) {
    val owner = calendar.organizerAttendeeUi ?: return
    participants.removeAll { it is AttendeeUi && it.email == owner.email }
    organizerSource = OrganizerSource.Dynamic(owner)
}
