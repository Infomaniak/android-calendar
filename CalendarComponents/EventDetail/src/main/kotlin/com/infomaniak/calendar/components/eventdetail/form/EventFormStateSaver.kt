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
package com.infomaniak.calendar.components.eventdetail.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.infomaniak.calendar.components.eventdetail.models.EventColorChoice
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi
import com.infomaniak.calendar.components.foundation.models.LocationUi
import com.infomaniak.calendar.components.foundation.models.OrganizerSource
import com.infomaniak.calendar.components.eventdetail.state.TimeZoneSaver
import com.infomaniak.calendar.components.foundation.models.ParticipantUi
import kotlinx.datetime.LocalDateTime

private const val TITLE_KEY = "title"
private const val CALENDAR_KEY = "calendar"
private const val EVENT_COLOR_KEY = "eventColor"
private const val IS_ALL_DAY_KEY = "isAllDay"
private const val START_KEY = "start"
private const val START_TIME_ZONE_KEY = "startTimeZone"
private const val END_KEY = "end"
private const val END_TIME_ZONE_KEY = "endTimeZone"
private const val HAS_END_EVER_BEEN_TOUCHED_KEY = "hasEndEverBeenTouched"
private const val LOCATION_KEY = "location"
private const val RECURRENCE_KEY = "recurrence"
private const val PARTICIPANTS_KEY = "participants"
private const val ORGANIZER_SOURCE_KEY = "organizerSource"
private const val IS_KMEET_URL_ENABLED_KEY = "isKMeetUrlEnabled"
private const val NOTIFICATIONS_KEY = "notifications"
private const val IS_OCCUPIED_KEY = "isOccupied"
private const val IS_PUBLIC_KEY = "isPublic"

private val notificationsSaver = autoSaver<SnapshotStateList<EventDetailUi.Notification>>()

/** Saves every field of an [EventFormState] but its calendars: restored empty, they are left to its owner. */
val EventFormState.Companion.Saver: Saver<EventFormState, Any> get() = saver

private val saver = mapSaver(
    save = {
        mapOf(
            TITLE_KEY to with(TextFieldState.Saver) { save(it.titleTextState) },
            CALENDAR_KEY to it.calendar,
            EVENT_COLOR_KEY to it.eventColorState.value,
            IS_ALL_DAY_KEY to it.isAllDay.value,
            START_KEY to it.start,
            START_TIME_ZONE_KEY to with(TimeZoneSaver) { save(it.dateAndTimeState.start.timeZone) },
            END_KEY to it.end,
            END_TIME_ZONE_KEY to with(TimeZoneSaver) { save(it.dateAndTimeState.end.timeZone) },
            HAS_END_EVER_BEEN_TOUCHED_KEY to it.dateAndTimeState.hasEndEverBeenTouched.value,
            LOCATION_KEY to it.location,
            RECURRENCE_KEY to it.recurrence.value,
            // TODO: Use autoSaver once on Compose 1.13+, SnapshotStateSet only parcels its first element before (b/537461421)
            PARTICIPANTS_KEY to ArrayList(it.participants),
            ORGANIZER_SOURCE_KEY to it.organizerSource,
            IS_KMEET_URL_ENABLED_KEY to it.isKMeetUrlEnabled.value,
            NOTIFICATIONS_KEY to with(notificationsSaver) { save(it.notifications) },
            IS_OCCUPIED_KEY to it.isOccupied.value,
            IS_PUBLIC_KEY to it.isPublic.value,
        )
    },
    restore = {
        @Suppress("UNCHECKED_CAST")
        EventFormState(
            titleTextState = TextFieldState.Saver.restore(it[TITLE_KEY]!!)!!,
            calendar = it[CALENDAR_KEY] as EventDetailCalendar,
            eventColor = it[EVENT_COLOR_KEY] as EventColorChoice,
            isAllDay = it[IS_ALL_DAY_KEY] as Boolean,
            start = it[START_KEY] as LocalDateTime,
            startTimeZone = TimeZoneSaver.restore(it[START_TIME_ZONE_KEY] as String)!!,
            end = it[END_KEY] as LocalDateTime,
            endTimeZone = TimeZoneSaver.restore(it[END_TIME_ZONE_KEY] as String)!!,
            hasEndEverBeenTouched = it[HAS_END_EVER_BEEN_TOUCHED_KEY] as Boolean,
            location = it[LOCATION_KEY] as LocationUi?,
            recurrence = it[RECURRENCE_KEY] as RecurrenceUi,
            participants = (it[PARTICIPANTS_KEY] as ArrayList<ParticipantUi>).toSet(),
            organizerSource = it[ORGANIZER_SOURCE_KEY] as OrganizerSource?,
            isKMeetUrlEnabled = it[IS_KMEET_URL_ENABLED_KEY] as Boolean,
            notifications = notificationsSaver.restore(it[NOTIFICATIONS_KEY]!!)!!,
            isOccupied = it[IS_OCCUPIED_KEY] as Boolean,
            isPublic = it[IS_PUBLIC_KEY] as Boolean,
            calendars = emptyList(),
        )
    },
)
