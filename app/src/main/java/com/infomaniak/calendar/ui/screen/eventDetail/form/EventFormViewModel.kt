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
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.infomaniak.calendar.components.eventdetail.form.EventFormValues
import com.infomaniak.calendar.components.eventdetail.form.calendarOf
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.OrganizerSource
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.data.SettingsDataValues
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.screen.eventDetail.EventCreationCalendarsUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.screen.eventDetail.EventLoader
import com.infomaniak.calendar.utils.saveableAttendeesSearchState
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
 * Holds the values of an event form, shared with the screens it opens through its ViewModelStore.
 *
 * The values are saved as a whole in the [SavedStateHandle]. Restored ones win over the loaded ones, which are only read for a
 * new form. The calendars it can be put in are reloaded every time, never saved.
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
    var values: EventFormValues? by mutableStateOf(savedStateHandle.restoredValues())
        private set

    var calendars: List<EventDetailCalendarGroup>? by mutableStateOf(null)
        private set

    var attendeesSearchState: AttendeesSearchState<OrganizerSource>? by mutableStateOf(null)
        private set

    init {
        savedStateHandle.setSavedStateProvider(VALUES_KEY) {
            values?.let { encodeToSavedState(EventFormValues.serializer(), it) } ?: savedState()
        }

        when (destination) {
            is NavDestination.EventCreation -> viewModelScope.launch {
                val attendeesSearchState = savedStateHandle.saveableAttendeesSearchState<OrganizerSource>()
                val (creationCalendars, defaultEventDuration) = combine(
                    eventCreationCalendarsUseCase.creationCalendars.filterNotNull(),
                    settingsDataValues.defaultEventDuration.flow,
                    ::Pair,
                ).first()
                show(creationCalendars.calendars, attendeesSearchState) {
                    EventFormValues.default(
                        calendarId = creationCalendars.initialCalendar.id,
                        date = destination.date,
                        timeZone = TimeZone.currentSystemDefault(),
                        eventDuration = defaultEventDuration,
                    )
                }

                snapshotFlow { values?.calendarId?.let(creationCalendars.calendars::calendarOf) }
                    .filterNotNull()
                    .collect(attendeesSearchState::followOrganizerOf)
            }
            is NavDestination.EventEdit -> if (loadedEvent != null) {
                showEdit(loadedEvent)
            } else {
                viewModelScope.launch { showEdit(loadEvent(destination)) }
            }
        }
    }

    fun update(transform: (EventFormValues) -> EventFormValues) {
        values = values?.let(transform)
    }

    fun toEventDraft(): EventDraft? {
        val values = values ?: return null
        val calendar = calendars?.calendarOf(values.calendarId) ?: return null
        val attendeesSearchState = attendeesSearchState ?: return null
        val initialOrganizer = (attendeesSearchState.organizerSource as? OrganizerSource.Static)?.organizer

        return values.toEventDraft(calendar, attendeesSearchState.attendees, initialOrganizer)
    }

    private suspend fun loadEvent(destination: NavDestination.EventEdit): EventDetailUi {
        return eventLoader.eventDetailUi(eventLoader.eventAndCalendar(destination.occurrenceId))
            .filterIsInstance<EventDetailUiState.Displayable.Success>()
            .first()
            .eventDetail
    }

    private fun showEdit(event: EventDetailUi) {
        val attendeesSearchState = savedStateHandle.saveableAttendeesSearchState<OrganizerSource>(
            initialParticipants = event.attendees.all,
            initialOrganizerSource = OrganizerSource.Static(event.organizer),
        )
        show(event.calendars, attendeesSearchState, event::toEventFormValues)
    }

    private fun show(
        calendars: List<EventDetailCalendarGroup>,
        attendeesSearchState: AttendeesSearchState<OrganizerSource>,
        initialValues: () -> EventFormValues,
    ) {
        this.calendars = calendars
        this.attendeesSearchState = attendeesSearchState
        if (values == null) values = initialValues()
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

    companion object {
        private const val VALUES_KEY = "values"

        private fun SavedStateHandle.restoredValues(): EventFormValues? {
            val savedValues = get<SavedState>(VALUES_KEY)?.takeUnless { it.read { isEmpty() } } ?: return null
            return decodeFromSavedState(EventFormValues.serializer(), savedValues)
        }
    }
}

/** Organizing an event as the owner of its calendar, who must then not also be one of its attendees. */
private fun AttendeesSearchState<OrganizerSource>.followOrganizerOf(calendar: EventDetailCalendar) {
    val owner = calendar.organizerAttendeeUi ?: return
    participants.removeAll { it is AttendeeUi && it.email == owner.email }
    organizerSource = OrganizerSource.Dynamic(owner)
}
