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
package com.infomaniak.calendar.ui.screen.eventDetail.model

import com.infomaniak.calendar.components.eventdetail.form.rememberSaveableEventFormState
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar

/** The calendars an event form can be filled with, in the shape expected by [rememberSaveableEventFormState]. */
data class EventFormCalendars(val calendars: List<EventDetailCalendar>, val initialCalendar: EventDetailCalendar)
