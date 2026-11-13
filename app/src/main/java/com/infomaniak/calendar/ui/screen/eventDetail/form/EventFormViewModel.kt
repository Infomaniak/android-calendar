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

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.form.Saver
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.foundation.models.OrganizerSource.Static
import com.infomaniak.calendar.data.SettingsDataValues
import com.infomaniak.calendar.ui.screen.eventDetail.EventCreationCalendarsUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus

/**
 * Holds the [EventFormState] of an event form, shared with the screens it opens through its ViewModelStore.
 *
 * The form is saved as a whole in the [SavedStateHandle], so the screens it opens find it even when restored alone. The form's
 * screen initializes it: a restored form wins over the loaded one, and the calendars it can be put in are observed, never saved.
 */
@OptIn(SavedStateHandleSaveableApi::class)
@AssistedInject
class EventFormViewModel(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val eventCreationCalendarsUseCase: EventCreationCalendarsUseCase,
    private val settingsDataValues: SettingsDataValues,
) : ViewModel() {
    /** The form once loaded, for the screens it opens. */
    var formState: EventFormState? by savedStateHandle.saveable(FORM_KEY, stateSaver = NullableFormSaver) { mutableStateOf(null) }
        private set

    private var isInitialized = false

    fun initCreation(date: LocalDate) {
        if (isInitialized) return
        isInitialized = true

        viewModelScope.launch {
            if (formState != null) return@launch
            val creationCalendars = eventCreationCalendarsUseCase.creationCalendars.filterNotNull().first()
            val defaultEventDuration = settingsDataValues.defaultEventDuration.flow.first()

            formState = EventFormState.default(
                calendar = creationCalendars.initialCalendar,
                date = date,
                timeZone = TimeZone.currentSystemDefault(),
                eventDuration = defaultEventDuration,
                calendars = creationCalendars.calendars,
            )
        }
    }

    /** @param event The edited event, already loaded by the event detail, kept up to date here afterwards. */
    fun initEdition(event: EventDetailUi) {
        if (isInitialized) return
        isInitialized = true

        val start = event.start.atTimeZone
        val end = event.end.atTimeZone
        // KMP ends an all-day event on the day after it, the form shows its last day
        val endDate = if (event.isAllDay) end.date.minus(1, DateTimeUnit.DAY) else end.date
        formState = EventFormState(
            titleTextState = TextFieldState(event.title),
            calendar = event.calendar,
            eventColor = event.eventColorChoice,
            isAllDay = event.isAllDay,
            start = start,
            startTimeZone = event.start.timeZone,
            end = endDate.atTime(end.time),
            endTimeZone = event.end.timeZone,
            hasEndEverBeenTouched = false,
            location = event.location,
            participants = event.attendees.all.toSet(),
            organizerSource = Static(event.organizer),
            isKMeetUrlEnabled = event.kMeetUrl != null,
            notifications = event.notifications,
            isOccupied = event.isOccupied,
            isPublic = event.classification == EventDetailUi.Classification.Public,
            calendars = event.calendars,
        )
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

        private val NullableFormSaver = Saver<EventFormState?, Any>(
            save = { formState -> formState?.let { with(EventFormState.Saver) { save(it) } } },
            restore = EventFormState.Saver::restore,
        )
    }
}

private val EventDetailTiming.timeZone: TimeZone
    get() = (this as? EventDetailTiming.Precise)?.timeZone ?: TimeZone.currentSystemDefault()
