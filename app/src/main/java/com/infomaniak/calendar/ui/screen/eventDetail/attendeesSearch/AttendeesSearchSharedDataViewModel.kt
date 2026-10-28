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
package com.infomaniak.calendar.ui.screen.eventDetail.attendeesSearch

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.ui.navigation.SharedViewModelStoreNavEntryDecorator
import com.infomaniak.calendar.utils.contactKey
import com.infomaniak.multiplatform_core.contacts.ContactsManager
import com.infomaniak.multiplatform_core.contacts.domain.model.Contact
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shares the [AttendeesSearchState] instance between the parent screen and the attendees search screen.
 *
 * To do so, we explicitly share the instance of the view model between the two screens using a [SharedViewModelStoreNavEntryDecorator].
 */
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class AttendeesSearchSharedDataViewModel(private val contactsManager: ContactsManager) : ViewModel() {
    private val _attendeesSearchState: MutableStateFlow<AttendeesSearchUiState> = MutableStateFlow(AttendeesSearchUiState.Loading)
    val attendeesSearchState: StateFlow<AttendeesSearchUiState> = _attendeesSearchState.asStateFlow()

    fun setAttendeesSearchState(attendeesSearchState: AttendeesSearchState) {
        _attendeesSearchState.value = AttendeesSearchUiState.Success(attendeesSearchState)
    }

    suspend fun search(query: String) {
        val results = contactsManager.search(query)
        val uiState = attendeesSearchState.value as? AttendeesSearchUiState.Success ?: return
        uiState.state.results.clear()
        uiState.state.results.addAll(results.toAttendeesUi(uiState.alreadyPresentAttendees))
    }
}

private fun List<Contact>.toAttendeesUi(alreadyPresentAttendees: Map<String, AttendeeUi>): List<AttendeeUi> {
    return map { contact ->
        val alreadyPresentAttendee = alreadyPresentAttendees[contact.contactKey]
        if (alreadyPresentAttendee != null) return@map alreadyPresentAttendee

        return@map AttendeeUi(
            key = contact.contactKey,
            displayName = contact.name,
            email = contact.email,
            status = null,
            isOrganizer = false,
        )
    }
}

sealed interface AttendeesSearchUiState {
    /** The parent screen hasn't provided its [AttendeesSearchState] yet. */
    data object Loading : AttendeesSearchUiState
    data class Success(val state: AttendeesSearchState) : AttendeesSearchUiState {
        /** Cached by the snapshot system: recomputed only when [AttendeesSearchState.attendees] changes. */
        val alreadyPresentAttendees: Map<String, AttendeeUi> by derivedStateOf { state.attendees.associateBy { it.contactKey } }
    }
}
