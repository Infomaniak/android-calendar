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
package com.infomaniak.calendar.components.foundation.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.AttendeesSearchResult
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.core.common.utils.isEmailRfc5321Compliant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.infomaniak.core.common.R as RCore

@Stable
class AttendeesSearchState(
    val searchQueryTextFieldState: TextFieldState,
    val results: SnapshotStateList<AttendeesSearchResult>,
    val attendees: SnapshotStateSet<AttendeeUi>,
) {
    val attendeesEmails by derivedStateOf { attendees.map { it.email } }

    fun addAttendee(attendee: AttendeeUi) {
        addRawAttendee(attendee.copy(status = ParticipationStatus.NeedsAction))
    }

    fun removeAttendee(attendee: AttendeeUi) {
        attendees.remove(attendee)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addCustomAttendee(email: String = searchQueryTextFieldState.text.toString().trim()): CustomAttendeeResult {
        if (email in attendeesEmails) return CustomAttendeeResult.AlreadyPresent
        if (!email.isEmailRfc5321Compliant()) return CustomAttendeeResult.InvalidEmail

        addRawAttendee(
            AttendeeUi(
                key = Uuid.generateV7().toString(),
                email = email,
                displayName = null,
                status = ParticipationStatus.NeedsAction,
            ),
        )

        return CustomAttendeeResult.Added
    }

    private fun addRawAttendee(attendee: AttendeeUi) {
        attendees.add(attendee)
        searchQueryTextFieldState.clearText()
    }

    enum class CustomAttendeeResult(val messageRes: Int? = null) {
        Added,
        AlreadyPresent(RCore.string.emailAlreadyUsed),
        InvalidEmail(RCore.string.emailInvalid),
    }

    object Saver : androidx.compose.runtime.saveable.Saver<AttendeesSearchState, List<Any>> {
        private val resultsSaver = autoSaver<SnapshotStateList<AttendeesSearchResult>>()
        // TODO: Use autoSaver once on Compose 1.13+, SnapshotStateSet only parcels its first element before (b/537461421)
        private val attendeesSaver = androidx.compose.runtime.saveable.Saver<SnapshotStateSet<AttendeeUi>, ArrayList<AttendeeUi>>(
            save = { ArrayList(it) },
            restore = { mutableStateSetOf<AttendeeUi>().apply { addAll(it) } },
        )

        // TextFieldState.Saver.restore() never returns null, and neither do resultsSaver and attendeesSaver
        override fun SaverScope.save(value: AttendeesSearchState): List<Any> = listOf(
            with(TextFieldState.Saver) { save(value.searchQueryTextFieldState)!! },
            with(resultsSaver) { save(value.results)!! },
            with(attendeesSaver) { save(value.attendees)!! },
        )

        @Suppress("UNCHECKED_CAST")
        override fun restore(value: List<Any>): AttendeesSearchState = AttendeesSearchState(
            searchQueryTextFieldState = TextFieldState.Saver.restore(value[0])!!,
            results = resultsSaver.restore(value[1])!!,
            attendees = attendeesSaver.restore(value[2] as ArrayList<AttendeeUi>)!!,
        )
    }
}

/**
 * Remembers saveable attendee-search state initialized from attendees, results, and query.
 *
 * Mutations to its snapshot collections update consumers and are restored after state recreation.
 */
@Composable
fun rememberSaveableAttendeesSearchState(
    attendees: SnapshotStateSet<AttendeeUi> = rememberSaveable { mutableStateSetOf() },
    results: SnapshotStateList<AttendeesSearchResult> = rememberSaveable { mutableStateListOf() },
    searchQuery: String = "",
): AttendeesSearchState = rememberSaveable(saver = AttendeesSearchState.Saver) {
    AttendeesSearchState(
        searchQueryTextFieldState = TextFieldState(initialText = searchQuery),
        results = results,
        attendees = attendees,
    )
}
