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
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.AvatarStrategy
import com.infomaniak.calendar.components.foundation.models.OrganizerSource
import com.infomaniak.calendar.components.foundation.models.OrganizerSource.Static
import com.infomaniak.calendar.components.foundation.models.ParticipantUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.utils.addParticipant
import com.infomaniak.calendar.data.SettingsDataValues
import com.infomaniak.calendar.ui.screen.eventDetail.EventCreationCalendarsUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.screen.eventDetail.EventLoader
import com.infomaniak.calendar.ui.screen.eventDetail.creation.EventCreationMode
import com.infomaniak.calendar.ui.screen.eventDetail.model.EventCreationCalendars
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
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
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

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
    private val eventLoader: EventLoader,
    private val settingsDataValues: SettingsDataValues,
) : ViewModel() {
    /** The form once loaded, for the screens it opens. */
    var formState: EventFormState? by savedStateHandle.saveable(FORM_KEY, stateSaver = NullableFormSaver) { mutableStateOf(null) }
        private set

    private var isInitialized = false

    fun initCreation(mode: EventCreationMode) {
        if (isInitialized) return
        isInitialized = true

        viewModelScope.launch {
            if (formState != null) return@launch
            val creationCalendars = eventCreationCalendarsUseCase.creationCalendars.filterNotNull().first()

            formState = when (mode) {
                is EventCreationMode.Empty -> emptyForm(mode.date, creationCalendars)
                is EventCreationMode.Duplicate -> duplicateForm(mode.occurrenceId, mode.date, creationCalendars)
            }
        }
    }

    /** @param event The edited event, already loaded by the event detail, kept up to date here afterward. */
    fun initEdition(event: EventDetailUi) {
        if (isInitialized) return
        isInitialized = true

        formState = event.toFormState(
            startDate = event.start.atTimeZone.date,
            calendar = event.calendar,
            calendars = event.calendars,
            participants = event.attendees.all.toSet(),
            organizerSource = Static(event.organizer),
        )
    }

    private suspend fun emptyForm(date: LocalDate, creationCalendars: EventCreationCalendars): EventFormState {
        return EventFormState.default(
            calendar = creationCalendars.initialCalendar,
            date = date,
            timeZone = TimeZone.currentSystemDefault(),
            eventDuration = settingsDataValues.defaultEventDuration.flow.first(),
            calendars = creationCalendars.calendars,
        )
    }

    /** A copy of the event of [occurrenceId] moved to [date], or an empty form when that event no longer exists. */
    private suspend fun duplicateForm(
        occurrenceId: OccurrenceId,
        date: LocalDate,
        creationCalendars: EventCreationCalendars,
    ): EventFormState {
        val eventState = eventLoader
            .eventDetailUi(eventLoader.eventAndCalendar(occurrenceId))
            .first { it != EventDetailUiState.Displayable.Loading }
        val event = (eventState as? EventDetailUiState.Displayable.Success)?.eventDetail
            ?: return emptyForm(date, creationCalendars)

        val eventCalendar = creationCalendars
            .calendars
            .flatMap { it.calendars }
            .find { it.id == event.calendar.id }
            ?: creationCalendars.initialCalendar
        val organizerSource = eventCalendar.organizerAttendeeUi?.let(OrganizerSource::Dynamic)

        val participants = buildSet {
            // Invites the attendees anew, without their replies nor organizer role, the owner of calendar organizes the copy as Me
            event.invitees().forEach {
                addParticipant(it.copy(status = ParticipationStatus.NeedsAction, isOrganizer = false), organizerSource)
            }
        }

        return event.toFormState(
            startDate = date,
            calendar = eventCalendar,
            calendars = creationCalendars.calendars,
            participants = participants,
            organizerSource = organizerSource,
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

/** The form of the event, moved to [startDate] with the same start time and duration. */
private fun EventDetailUi.toFormState(
    startDate: LocalDate,
    calendar: EventDetailCalendar,
    calendars: List<EventDetailCalendarGroup>,
    participants: Set<ParticipantUi>,
    organizerSource: OrganizerSource?,
): EventFormState {
    val startDateTime = start.atTimeZone
    val endDateTime = end.atTimeZone
    val dayShift = startDateTime.date.daysUntil(startDate)
    // KMP ends an all-day event on the day after it, the form shows its last day
    val endDate = if (isAllDay) endDateTime.date.minus(1, DateTimeUnit.DAY) else endDateTime.date

    return EventFormState(
        titleTextState = TextFieldState(title),
        calendar = calendar,
        eventColor = eventColorChoice,
        isAllDay = isAllDay,
        start = startDate.atTime(startDateTime.time),
        startTimeZone = start.timeZone,
        end = endDate.plus(dayShift, DateTimeUnit.DAY).atTime(endDateTime.time),
        endTimeZone = end.timeZone,
        hasEndEverBeenTouched = false,
        location = location,
        participants = participants,
        organizerSource = organizerSource,
        isKMeetUrlEnabled = kMeetUrl != null,
        descriptionTextState = TextFieldState(description.orEmpty()),
        reminders = reminders,
        isOccupied = isOccupied,
        isPublic = classification == EventDetailUi.Classification.Public,
        calendars = calendars,
    )
}

/** The attendees of the event, led by its organizer when the attendees leave the organizer out. */
private fun EventDetailUi.invitees(): List<AttendeeUi> {
    val organizer = organizer ?: return attendees.all
    if (attendees.all.any { it.email.equals(organizer.email, ignoreCase = true) }) return attendees.all

    val organizerAttendee = AttendeeUi(
        email = organizer.email,
        displayName = organizer.displayName,
        status = ParticipationStatus.Accepted,
        avatar = AvatarStrategy.ToCompute,
        isOrganizer = true,
    )
    return listOf(organizerAttendee) + attendees.all
}

private val EventDetailTiming.timeZone: TimeZone
    get() = (this as? EventDetailTiming.Precise)?.timeZone ?: TimeZone.currentSystemDefault()
