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
package com.infomaniak.calendar.ui.screen.eventDetail.creation

import androidx.compose.foundation.layout.plus
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.eventdetail.form.EventForm
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.form.rememberSaveableEventFormState
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.ui.theme.Dimens

@Composable
fun EventCreationScreen(modifier: Modifier = Modifier, viewModel: EventCreationViewModel = viewModel()) {
    val eventFormCalendars = viewModel.eventFormCalendars.collectAsStateWithLifecycle().value
    val state = rememberSaveableEventFormState(
        calendars = eventFormCalendars?.calendars ?: emptyList(),
        initialCalendar = eventFormCalendars?.initialCalendar,
    )

    EventCreationScreen(
        state = state,
        onSubmit = { viewModel.submitEvent(state.toEventDraft() ?: return@EventCreationScreen) },
        modifier = modifier,
    )
}

@Composable
private fun EventCreationScreen(
    state: EventFormState,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("EventCreationScreen") }) },
        modifier = modifier,
    ) { contentPadding ->
        EventForm(
            state = state,
            contentPadding = contentPadding + Dimens.EventDetailScreensHorizontalPadding,
        )
    }
}

@Preview
@Composable
private fun EventCreationScreenPreview() {
    CalendarThemeForPreview {
        val state = rememberSaveableEventFormState(
            calendars = listOf(previewEventDetailCalendar),
            initialCalendar = previewEventDetailCalendar,
        )

        EventCreationScreen(state = state, onSubmit = {})
    }
}
