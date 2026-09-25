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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import com.infomaniak.calendar.components.eventdetail.models.EventDetailCalendar
import com.infomaniak.calendar.components.eventdetail.models.EventDraft

@Stable
class EventFormState(
    val titleTextState: TextFieldState,
    val colorState: MutableState<Color?>,
    val calendars: List<EventDetailCalendar>,
) {
    fun toEventDraft(): EventDraft? = EventDraft(
        title = titleTextState.text.toString(),
        color = colorState.value ?: return null,
    )
}

@Composable
fun rememberSaveableEventFormState(
    calendars: List<EventDetailCalendar>,
    initialCalendar: EventDetailCalendar?,
    initialText: String = "",
): EventFormState {
    val textFieldState = rememberTextFieldState(initialText)
    val colorState = rememberSaveable(initialCalendar == null, stateSaver = ColorSaver) { mutableStateOf(initialCalendar?.color) }

    return EventFormState(
        titleTextState = textFieldState,
        colorState = colorState,
        calendars = calendars,
    )
}

private val ColorSaver = Saver<Color?, String>(
    save = { color -> color?.value?.toString() ?: "null" },
    restore = { stringValue -> if (stringValue == "null") null else Color(stringValue.toULong()) },
)
