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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.AttendeesSearchResult
import com.infomaniak.calendar.components.foundation.models.AvatarStrategy
import com.infomaniak.calendar.components.foundation.state.AttendeesSearchState
import com.infomaniak.calendar.utils.avatarData
import com.infomaniak.multiplatform_core.contacts.ContactsManager
import com.infomaniak.multiplatform_core.contacts.domain.model.Contact
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.collectLatest

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class AttendeesSearchViewModel(private val contactsManager: ContactsManager) : ViewModel() {
    @Composable
    fun EnableSearch(attendeesSearchState: AttendeesSearchState<*>) {
        val textFieldState = attendeesSearchState.searchQueryTextFieldState
        LaunchedEffect(textFieldState) {
            snapshotFlow { textFieldState.text }.collectLatest {
                search(attendeesSearchState, it.toString())
            }
        }
    }

    private suspend fun search(attendeesSearchState: AttendeesSearchState<*>, query: String) {
        if (query.isBlank()) {
            attendeesSearchState.results.clear()
        } else {
            val results = contactsManager.search(query)
            attendeesSearchState.results.clear()
            attendeesSearchState.results.addAll(
                results.toAttendeesUi(attendeesSearchState.alreadyPresentAttendees) + AttendeesSearchResult.AddCustomAttendee,
            )
        }
    }
}

private fun List<Contact>.toAttendeesUi(alreadyPresentAttendees: Map<AttendeeUi.Key, AttendeeUi>): List<AttendeeUi> {
    return map { contact ->
        val alreadyPresentAttendee = alreadyPresentAttendees[AttendeeUi.Key(contact.email, contact.name)]
        if (alreadyPresentAttendee != null) return@map alreadyPresentAttendee

        return@map AttendeeUi(
            displayName = contact.name,
            email = contact.email,
            status = null,
            avatar = AvatarStrategy.Precomputed(contact.avatarData),
            isOrganizer = false,
        )
    }
}
