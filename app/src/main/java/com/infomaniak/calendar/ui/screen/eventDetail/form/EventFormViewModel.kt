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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.models.EventColorChoice
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.state.DateAndTimeState
import com.infomaniak.calendar.components.eventdetail.state.TimeZoneSaver
import com.infomaniak.calendar.components.eventdetail.state.defaultEventStartAndEnd
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.OrganizerSource
import com.infomaniak.calendar.components.foundation.models.ParticipantUi
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.data.SettingsDataValues
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.screen.eventDetail.EventCreationCalendarsUseCase
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.screen.eventDetail.EventLoader
import com.infomaniak.calendar.utils.saveableAttendeesSearchState
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

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
 * The state is built once its initial values are loaded. Every field is saved in the [SavedStateHandle], whose restored values
 * win over the loaded ones.
 */
@OptIn(SavedStateHandleSaveableApi::class)
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
            is NavDestination.EventCreation -> viewModelScope.launch { initCreation(destination) }
            is NavDestination.EventEdit -> if (loadedEvent != null) {
                initEdit(loadedEvent)
            } else {
                viewModelScope.launch { initEdit(loadEvent(destination)) }
            }
        }
    }

    private suspend fun initCreation(destination: NavDestination.EventCreation) {
        val (creationCalendars, defaultEventDuration) = combine(
            eventCreationCalendarsUseCase.creationCalendars.filterNotNull(),
            settingsDataValues.defaultEventDuration.flow,
            ::Pair,
        ).first()
        val timeZone = TimeZone.currentSystemDefault()
        val (start, end) = defaultEventStartAndEnd(destination.date, timeZone, defaultEventDuration)

        val attendeesSearchState = savedStateHandle.saveableAttendeesSearchState<OrganizerSource>()
        val state = buildState(
            InitialValues(
                calendars = creationCalendars.calendars,
                calendar = creationCalendars.initialCalendar,
                eventColor = EventColorChoice.Default,
                title = "",
                isAllDay = false,
                start = start,
                startTimeZone = timeZone,
                end = end,
                endTimeZone = timeZone,
                isKMeetUrlEnabled = false,
                notifications = emptyList(),
                isOccupied = true,
                isPublic = true,
            ),
            attendeesSearchState = attendeesSearchState,
        )
        this.attendeesSearchState = attendeesSearchState
        this.state = state

        snapshotFlow { state.calendar }.collect { calendar -> attendeesSearchState.followOrganizerOf(calendar) }
    }

    private suspend fun loadEvent(destination: NavDestination.EventEdit): EventDetailUi {
        return eventLoader.eventDetailUi(eventLoader.eventAndCalendar(destination.occurrenceId))
            .filterIsInstance<EventDetailUiState.Displayable.Success>()
            .first()
            .eventDetail
    }

    private fun initEdit(event: EventDetailUi) {
        val start = event.start.atTimeZone
        val end = event.end.atTimeZone

        val attendeesSearchState = savedStateHandle.saveableAttendeesSearchState<OrganizerSource>(
            initialParticipants = event.attendees.all,
            initialOrganizerSource = OrganizerSource.Static(event.organizer),
        )
        state = buildState(
            InitialValues(
                calendars = event.calendars,
                calendar = event.calendar,
                eventColor = event.eventColorChoice,
                title = event.title,
                isAllDay = event.isAllDay,
                start = start,
                startTimeZone = event.start.timeZone,
                // KMP ends an all-day event on the day after it, the form shows its last day
                end = if (event.isAllDay) LocalDateTime(end.date.minus(1, DateTimeUnit.DAY), end.time) else end,
                endTimeZone = event.end.timeZone,
                isKMeetUrlEnabled = event.kMeetUrl != null,
                notifications = event.notifications,
                isOccupied = event.isOccupied,
                isPublic = event.classification == EventDetailUi.Classification.Public,
            ),
            attendeesSearchState = attendeesSearchState,
        )
        this.attendeesSearchState = attendeesSearchState
    }

    private fun buildState(initial: InitialValues, attendeesSearchState: AttendeesSearchState<OrganizerSource>): EventFormState {
        return EventFormState(
            titleTextState = savedStateHandle.saveable(TITLE_KEY, saver = TextFieldState.Saver) { TextFieldState(initial.title) },
            eventColorState = saveableState(EVENT_COLOR_KEY) { initial.eventColor },
            selectedCalendarState = saveableState(CALENDAR_KEY) { initial.calendar },
            isAllDay = saveableState(IS_ALL_DAY_KEY) { initial.isAllDay },
            dateAndTimeState = DateAndTimeState(
                start = DateAndTimeState.Entry(
                    date = saveableState(START_DATE_KEY) { initial.start.date },
                    time = saveableState(START_TIME_KEY) { initial.start.time },
                    timeZone = savedStateHandle.saveable(START_TIME_ZONE_KEY, stateSaver = TimeZoneSaver) { mutableStateOf(initial.startTimeZone) },
                ),
                end = DateAndTimeState.Entry(
                    date = saveableState(END_DATE_KEY) { initial.end.date },
                    time = saveableState(END_TIME_KEY) { initial.end.time },
                    timeZone = savedStateHandle.saveable(END_TIME_ZONE_KEY, stateSaver = TimeZoneSaver) { mutableStateOf(initial.endTimeZone) },
                ),
                hasEndEverBeenTouched = saveableState(HAS_END_EVER_BEEN_TOUCHED_KEY) { false },
            ),
            attendees = { attendeesSearchState.attendees },
            initialOrganizer = (attendeesSearchState.organizerSource as? OrganizerSource.Static)?.organizer,
            isKMeetUrlEnabled = saveableState(IS_KMEET_URL_ENABLED_KEY) { initial.isKMeetUrlEnabled },
            notifications = saveableState(NOTIFICATIONS_KEY) { initial.notifications },
            isOccupied = saveableState(IS_OCCUPIED_KEY) { initial.isOccupied },
            isPublic = saveableState(IS_PUBLIC_KEY) { initial.isPublic },
            calendars = initial.calendars,
        )
    }

    private fun <T> saveableState(key: String, initialValue: () -> T): MutableState<T> {
        return savedStateHandle.saveable(key) { mutableStateOf(initialValue()) }
    }

    /** Organizing an event as the owner of its calendar, who must then not also be one of its attendees. */
    private fun AttendeesSearchState<OrganizerSource>.followOrganizerOf(calendar: EventDetailCalendar) {
        val owner = calendar.organizerAttendeeUi ?: return
        participants.removeAll { it is AttendeeUi && it.email == owner.email }
        organizerSource = OrganizerSource.Dynamic(owner)
    }

    private class InitialValues(
        val calendars: List<EventDetailCalendarGroup>,
        val calendar: EventDetailCalendar,
        val eventColor: EventColorChoice,
        val title: String,
        val isAllDay: Boolean,
        val start: LocalDateTime,
        val startTimeZone: TimeZone,
        val end: LocalDateTime,
        val endTimeZone: TimeZone,
        val isKMeetUrlEnabled: Boolean,
        val notifications: List<EventDetailUi.Notification>,
        val isOccupied: Boolean,
        val isPublic: Boolean,
    )

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
        private const val TITLE_KEY = "title"
        private const val EVENT_COLOR_KEY = "eventColor"
        private const val CALENDAR_KEY = "calendar"
        private const val IS_ALL_DAY_KEY = "isAllDay"
        private const val START_DATE_KEY = "startDate"
        private const val START_TIME_KEY = "startTime"
        private const val START_TIME_ZONE_KEY = "startTimeZone"
        private const val END_DATE_KEY = "endDate"
        private const val END_TIME_KEY = "endTime"
        private const val END_TIME_ZONE_KEY = "endTimeZone"
        private const val HAS_END_EVER_BEEN_TOUCHED_KEY = "hasEndEverBeenTouched"
        private const val IS_KMEET_URL_ENABLED_KEY = "isKMeetUrlEnabled"
        private const val NOTIFICATIONS_KEY = "notifications"
        private const val IS_OCCUPIED_KEY = "isOccupied"
        private const val IS_PUBLIC_KEY = "isPublic"
    }
}

private val EventDetailTiming.timeZone: TimeZone
    get() = (this as? EventDetailTiming.Precise)?.timeZone ?: TimeZone.currentSystemDefault()
