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
package com.infomaniak.calendar.components.eventdetail.preview

import androidx.compose.ui.graphics.Color
import com.infomaniak.calendar.components.eventdetail.models.EventDetailAccount
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup

val previewEventDetailCalendar by lazy { EventDetailCalendar("Calendar 1", "Vacation", Color.Red) }

val previewEventDetailCalendarGroups by lazy {
    listOf(
        EventDetailCalendarGroup(
            account = EventDetailAccount(id = 1, name = "ellen.joe@ik.me"),
            calendars = listOf(previewEventDetailCalendar, EventDetailCalendar("Calendar 2", "Work", Color.Blue)),
        ),
        EventDetailCalendarGroup(
            account = EventDetailAccount(id = 2, name = "jane.doe@ik.me"),
            calendars = listOf(EventDetailCalendar("Calendar 3", "Family", Color.Green)),
        ),
    )
}
