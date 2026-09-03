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
package com.infomaniak.calendar.components.eventdetail

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.Room
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.filetypes.FileType
import kotlin.time.Instant

@Immutable
data class EventDetailUi(
    val eventColor: Color,
    val calendarColor: Color,
    val title: String,
    val start: EventDetailTiming,
    val end: EventDetailTiming,
    val isAllDay: Boolean,
    val attendees: Attendees,
    val kMeetUrl: String?,
    val location: String?,
    val room: Room?,
    val urlLink: String?,
    val description: String?,
    val files: List<File>,
    val notifications: List<Notification>,
) {
    @Immutable
    data class File(val name: String) {
        val fileType: FileType by lazy { FileType.guessFromFileName(name) }
    }

    @Immutable
    data class Notification(val type: Type, val executionTime: Instant) {
        @Immutable
        enum class Type(@DrawableRes val icon: Int, @StringRes val label: Int) {
            Email(R.drawable.ic_bell, R.string.notificationTypeEmail),
            Push(R.drawable.ic_bubble_top_right_circle, R.string.notificationTypePush),
        }
    }
}
