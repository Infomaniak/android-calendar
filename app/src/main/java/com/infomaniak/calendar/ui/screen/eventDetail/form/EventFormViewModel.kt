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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.form.EventFormValues
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.data.SettingsDataValues
import com.infomaniak.calendar.ui.screen.eventDetail.EventCreationCalendarsUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.screen.eventDetail.EventLoader
import com.infomaniak.calendar.utils.toEventFormValues
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * Holds the [EventFormValues] of an event form, shared with the screens it opens through its ViewModelStore.
 *
 * The form is saved as a whole in the [SavedStateHandle], so the screens it opens find it even when restored alone. The form's
 * screen initializes it: a restored form wins over the loaded one, and the calendars it can be put in are observed, never saved.
 */
@AssistedInject
class EventFormViewModel(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val eventLoader: EventLoader,
    private val eventCreationCalendarsUseCase: EventCreationCalendarsUseCase,
    private val settingsDataValues: SettingsDataValues,
) : ViewModel() {
    var uiState: EventFormUiState by mutableStateOf(
        savedStateHandle.restoredForm()?.let { EventFormUiState.Loaded(EventFormState(it, calendars = emptyList())) }
            ?: EventFormUiState.Loading,
    )
        private set

    /** The form once loaded, for the screens it opens. */
    val formState: EventFormState? get() = (uiState as? EventFormUiState.Loaded)?.formState

    private var isInitialized = false

    init {
        savedStateHandle.setSavedStateProvider(FORM_KEY) {
            formState?.let { encodeToSavedState(EventFormValues.serializer(), it.toValues()) } ?: savedState()
        }
    }

    fun initCreation(date: LocalDate) {
        if (isInitialized) return
        isInitialized = true

        viewModelScope.launch {
            if (formState != null) return@launch
            val creationCalendars = eventCreationCalendarsUseCase.creationCalendars.filterNotNull().first()
            val defaultEventDuration = settingsDataValues.defaultEventDuration.flow.first()

            uiState = EventFormUiState.Loaded(
                EventFormState(
                    EventFormValues.default(
                        calendar = creationCalendars.initialCalendar,
                        date = date,
                        timeZone = TimeZone.currentSystemDefault(),
                        eventDuration = defaultEventDuration,
                    ),
                    creationCalendars.calendars,
                ),
            )
        }
        viewModelScope.launch {
            eventCreationCalendarsUseCase.creationCalendars.collect { creationCalendars ->
                updateCalendars(creationCalendars?.calendars, fallback = creationCalendars?.initialCalendar)
            }
        }
    }

    /** @param event The edited event, already loaded by the event detail, kept up to date here afterwards. */
    fun initEdition(occurrenceId: OccurrenceId, event: EventDetailUi) {
        if (isInitialized) return
        isInitialized = true

        initForm(event)
        viewModelScope.launch {
            eventLoader.eventDetailUi(eventLoader.eventAndCalendar(occurrenceId))
                .filterIsInstance<EventDetailUiState.Displayable.Success>()
                .map { it.eventDetail }
                .collect(::initForm)
        }
    }

    private fun initForm(event: EventDetailUi) {
        if (formState == null) uiState = EventFormUiState.Loaded(EventFormState(event.toEventFormValues(), event.calendars))
        updateCalendars(event.calendars, fallback = event.calendar)
    }

    /** Keeps the calendars up to date, moving the form to [fallback] when its calendar can't be written to anymore. */
    private fun updateCalendars(calendars: List<EventDetailCalendarGroup>?, fallback: EventDetailCalendar?) {
        val formState = formState ?: return
        formState.calendars = calendars.orEmpty()
        if (calendars == null || fallback == null) return

        if (formState.calendar == null) formState.selectCalendar(fallback)
    }

    @AssistedFactory
    @ViewModelAssistedFactoryKey(EventFormViewModel::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): EventFormViewModel = create(extras.createSavedStateHandle())

        fun create(@Assisted savedStateHandle: SavedStateHandle): EventFormViewModel
    }

    companion object {
        private const val FORM_KEY = "form"

        private fun SavedStateHandle.restoredForm(): EventFormValues? {
            val savedValues = get<SavedState>(FORM_KEY)?.takeUnless { it.read { isEmpty() } } ?: return null
            return decodeFromSavedState(EventFormValues.serializer(), savedValues)
        }
    }
}
