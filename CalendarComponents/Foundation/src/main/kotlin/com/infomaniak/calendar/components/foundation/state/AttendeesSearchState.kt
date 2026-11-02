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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.AttendeesSearchResult
import com.infomaniak.calendar.components.foundation.models.OrganizerUi
import com.infomaniak.calendar.components.foundation.models.ParticipantUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.core.common.utils.isEmailRfc5321Compliant
import com.infomaniak.core.common.R as RCore

@Stable
class AttendeesSearchState(
    val searchQueryTextFieldState: TextFieldState,
    val results: SnapshotStateList<AttendeesSearchResult>,
    val participants: SnapshotStateSet<ParticipantUi>,
) {
    /** Snapshot of the edited event's organizer, taken once along with the [participants]. */
    var initialOrganizer: OrganizerUi? by mutableStateOf(null)

    /** What [ParticipantUi.Me] resolves to, the owner of the currently selected calendar. */
    var me: AttendeeUi? by mutableStateOf(null)

    val attendees: List<AttendeeUi> by derivedStateOf { participants.mapNotNull { it.resolve(me) } }

    val attendeesEmailsLowercase by derivedStateOf { attendees.map { it.email.lowercase() } }

    fun addAttendee(attendee: AttendeeUi) {
        innerAddAttendee(attendee.copy(status = ParticipationStatus.NeedsAction))
    }

    fun removeAttendee(attendee: AttendeeUi) {
        // A concrete entry wins over Me: when editing, our own attendee is concrete and shares Me's key
        participants.remove(attendee) || (attendee.key == me?.key && participants.remove(ParticipantUi.Me))
    }

    fun addCustomAttendee(email: String = searchQueryTextFieldState.text.toString().trim()): CustomAttendeeResult {
        if (email.lowercase() in attendeesEmailsLowercase) return CustomAttendeeResult.AlreadyPresent
        if (!email.isEmailRfc5321Compliant()) return CustomAttendeeResult.InvalidEmail

        innerAddAttendee(
            AttendeeUi(
                email = email,
                displayName = null,
                status = ParticipationStatus.NeedsAction,
            ),
        )

        return CustomAttendeeResult.Added
    }

    private fun innerAddAttendee(attendee: AttendeeUi) {
        if (participants.isEmpty()) participants.add(ParticipantUi.Me)
        participants.add(attendee)
        searchQueryTextFieldState.clearText()
    }

    enum class CustomAttendeeResult(val messageRes: Int? = null) {
        Added,
        AlreadyPresent(RCore.string.emailAlreadyUsed),
        InvalidEmail(RCore.string.emailInvalid),
    }

    object Saver : androidx.compose.runtime.saveable.Saver<AttendeesSearchState, List<Any?>> {
        private val resultsSaver = autoSaver<SnapshotStateList<AttendeesSearchResult>>()
        private val initialOrganizerSaver = autoSaver<OrganizerUi?>()
        private val meSaver = autoSaver<AttendeeUi?>()
        // TODO: Use autoSaver once on Compose 1.13+, SnapshotStateSet only parcels its first element before (b/537461421)
        private val participantsSaver =
            androidx.compose.runtime.saveable.Saver<SnapshotStateSet<ParticipantUi>, ArrayList<ParticipantUi>>(
                save = { ArrayList(it) },
                restore = { mutableStateSetOf<ParticipantUi>().apply { addAll(it) } },
            )

        // TextFieldState.Saver.restore() never returns null, and neither do resultsSaver and attendeesSaver
        override fun SaverScope.save(value: AttendeesSearchState): List<Any?> = listOf(
            with(TextFieldState.Saver) { save(value.searchQueryTextFieldState)!! },
            with(resultsSaver) { save(value.results)!! },
            with(participantsSaver) { save(value.participants)!! },
            with(initialOrganizerSaver) { save(value.initialOrganizer) },
            with(meSaver) { save(value.me) },
        )

        @Suppress("UNCHECKED_CAST")
        override fun restore(value: List<Any?>): AttendeesSearchState = AttendeesSearchState(
            searchQueryTextFieldState = TextFieldState.Saver.restore(value[0]!!)!!,
            results = resultsSaver.restore(value[1]!!)!!,
            participants = participantsSaver.restore(value[2] as ArrayList<ParticipantUi>)!!,
        ).apply {
            initialOrganizer = value[3]?.let(initialOrganizerSaver::restore)
            me = value[4]?.let(meSaver::restore)
        }
    }
}

/**
 * Remembers saveable attendee-search state initialized from attendees, results, and query.
 *
 * Mutations to its snapshot collections update consumers and are restored after state recreation.
 */
@Composable
fun rememberSaveableAttendeesSearchState(
    participants: SnapshotStateSet<ParticipantUi> = rememberSaveable { mutableStateSetOf() },
    results: SnapshotStateList<AttendeesSearchResult> = rememberSaveable { mutableStateListOf() },
    searchQuery: String = "",
): AttendeesSearchState = rememberSaveable(saver = AttendeesSearchState.Saver) {
    AttendeesSearchState(
        searchQueryTextFieldState = TextFieldState(initialText = searchQuery),
        results = results,
        participants = participants,
    )
}
