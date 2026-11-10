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
import com.infomaniak.calendar.components.eventdetail.form.EventForm
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.form.calendarOf
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.EventDraft
import com.infomaniak.calendar.data.SettingsDataValues
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.screen.eventDetail.EventCreationCalendarsUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.screen.eventDetail.EventLoader
import com.infomaniak.calendar.utils.toEventForm
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
 * Holds an [EventForm], shared with the screens it opens through its ViewModelStore.
 *
 * The form is saved as a whole in the [SavedStateHandle]. A restored one wins over the loaded one, which is only read for a new
 * form. The calendars it can be put in are reloaded every time, never saved.
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
    var formState: EventFormState? by mutableStateOf(savedStateHandle.restoredForm()?.let(::EventFormState))
        private set

    var calendars: List<EventDetailCalendarGroup>? by mutableStateOf(null)
        private set

    /** `false` while the form loads, and once its calendar can't be written to anymore. */
    val canSubmit: Boolean get() = formState?.form?.let { calendars?.calendarOf(it.calendarId) } != null

    init {
        savedStateHandle.setSavedStateProvider(FORM_KEY) {
            formState?.form?.let { encodeToSavedState(EventForm.serializer(), it) } ?: savedState()
        }

        when (destination) {
            is NavDestination.EventCreation -> {
                viewModelScope.launch { initForm(destination) }
                viewModelScope.launch {
                    eventCreationCalendarsUseCase.creationCalendars.collect { creationCalendars ->
                        updateCalendars(creationCalendars?.calendars, fallback = creationCalendars?.initialCalendar)
                    }
                }
            }
            is NavDestination.EventEdit -> {
                loadedEvent?.let(::initForm)
                viewModelScope.launch { eventDetails(destination).collect(::initForm) }
            }
        }
    }

    fun toEventDraft(): EventDraft? {
        val form = formState?.form ?: return null
        val calendar = calendars?.calendarOf(form.calendarId) ?: return null

        return form.toEventDraft(calendar)
    }

    private fun eventDetails(destination: NavDestination.EventEdit): Flow<EventDetailUi> {
        return eventLoader.eventDetailUi(eventLoader.eventAndCalendar(destination.occurrenceId))
            .filterIsInstance<EventDetailUiState.Displayable.Success>()
            .map { it.eventDetail }
    }

    private suspend fun initForm(destination: NavDestination.EventCreation) {
        if (formState != null) return
        val creationCalendars = eventCreationCalendarsUseCase.creationCalendars.filterNotNull().first()
        val defaultEventDuration = settingsDataValues.defaultEventDuration.flow.first()

        formState = EventFormState(
            EventForm.default(
                calendar = creationCalendars.initialCalendar,
                date = destination.date,
                timeZone = TimeZone.currentSystemDefault(),
                eventDuration = defaultEventDuration,
            ),
        )
    }

    private fun initForm(event: EventDetailUi) {
        if (formState == null) formState = EventFormState(event.toEventForm())
        updateCalendars(event.calendars, fallback = event.calendar)
    }

    /** Keeps the calendars up to date, moving the form to [fallback] when its calendar can't be written to anymore. */
    private fun updateCalendars(calendars: List<EventDetailCalendarGroup>?, fallback: EventDetailCalendar?) {
        this.calendars = calendars
        val formState = formState ?: return
        if (calendars == null || fallback == null) return

        if (calendars.calendarOf(formState.form.calendarId) == null) formState.edit { it.withCalendar(fallback) }
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
        private const val FORM_KEY = "form"

        private fun SavedStateHandle.restoredForm(): EventForm? {
            val savedValues = get<SavedState>(FORM_KEY)?.takeUnless { it.read { isEmpty() } } ?: return null
            return decodeFromSavedState(EventForm.serializer(), savedValues)
        }
    }
}
