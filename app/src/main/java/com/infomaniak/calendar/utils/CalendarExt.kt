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
package com.infomaniak.calendar.utils

import androidx.compose.ui.graphics.Color
import com.infomaniak.calendar.components.eventdetail.models.EventDetailAccount
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendarGroup
import com.infomaniak.calendar.utils.account.withCalendars
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.Calendar

fun Calendar.toEventDetailCalendar(): EventDetailCalendar = EventDetailCalendar(
    id = id.url,
    name = displayName,
    color = Color(colors.sourceColor),
)

/** Groups [calendars] under the email of their user's account, leaving out the accounts with none of them. */
fun List<User>.toEventDetailCalendarGroups(calendars: List<Calendar>): List<EventDetailCalendarGroup> =
    withCalendars(calendars).mapNotNull { (user, userCalendars) ->
        if (userCalendars.isEmpty()) return@mapNotNull null

        EventDetailCalendarGroup(
            account = EventDetailAccount(id = user.id, name = user.email),
            calendars = userCalendars.map(Calendar::toEventDetailCalendar),
        )
    }
