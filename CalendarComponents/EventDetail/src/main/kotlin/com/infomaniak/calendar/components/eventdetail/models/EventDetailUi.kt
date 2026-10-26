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
package com.infomaniak.calendar.components.eventdetail.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.filetypes.FileType
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Bell
import com.infomaniak.designsystem.icons.symbols.BubbleTopRightCircle
import com.infomaniak.designsystem.icons.symbols.Symbol

/**
 * @param calendars The calendars the event can be moved to: the writable ones, grouped by account.
 */
@Immutable
data class EventDetailUi(
    val eventColor: Color,
    val calendar: EventDetailCalendar,
    val calendars: List<EventDetailCalendarGroup>,
    val title: String,
    val start: EventDetailTiming,
    val end: EventDetailTiming,
    val isAllDay: Boolean,
    val recurrence: EventDetailRecurrence,
    val attendees: Attendees,
    val kMeetUrl: String?,
    val location: String?,
    val room: Room?,
    val urlLink: String?,
    val description: String?,
    val files: List<File>,
    val notifications: List<Notification>,
    val isOccupied: Boolean,
    val classification: Classification,
    val canEdit: Boolean,
) {
    val isRecurrent get() = recurrence is EventDetailRecurrence.Recurrence

    data class Room(val title: String, val seats: Int, val floor: Int)

    @Immutable
    data class File(val id: String, val name: String) {
        val fileType: FileType by lazy { FileType.guessFromFileName(name) }
    }

    @Immutable
    data class Notification(val type: Type, val time: NotificationTime) {
        @Immutable
        enum class Type(val icon: Symbol, @StringRes val label: Int) {
            Email(EsdsIcons.Bell, R.string.notificationTypeEmail),
            Push(EsdsIcons.BubbleTopRightCircle, R.string.notificationTypePush),
        }
    }

    @Immutable
    enum class Classification(@StringRes val label: Int) {
        Public(R.string.publicLabel),
        Private(R.string.privateLabel),
    }
}
