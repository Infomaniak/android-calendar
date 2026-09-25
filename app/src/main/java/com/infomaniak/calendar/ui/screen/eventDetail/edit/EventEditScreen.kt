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
package com.infomaniak.calendar.ui.screen.eventDetail.edit

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.infomaniak.calendar.components.eventdetail.form.EventForm
import com.infomaniak.calendar.components.eventdetail.form.EventFormState
import com.infomaniak.calendar.components.eventdetail.form.rememberSaveableEventFormState
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailCalendar
import com.infomaniak.calendar.ui.modifier.LocalSharedTransitionScope
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.theme.CalendarTheme
import com.infomaniak.calendar.ui.theme.Dimens
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId

@Composable
fun EventEditScreen(
    occurrenceId: OccurrenceId,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventEditViewModel = viewModel(),
) {
    val eventDetailUiState = viewModel.eventDetailUi.collectAsStateWithLifecycle().value
    val eventFormCalendars = viewModel.eventFormCalendars.collectAsStateWithLifecycle().value
    val uiState = when (eventDetailUiState) {
        EventDetailUiState.Loading -> EventEditScreenState.Loading
        is EventDetailUiState.Success -> EventEditScreenState.Success(
            rememberSaveableEventFormState(
                calendars = eventFormCalendars?.calendars ?: emptyList(),
                initialCalendar = eventFormCalendars?.initialCalendar,
                initialText = eventDetailUiState.eventDetail.title,
                initialAttendees = eventDetailUiState.eventDetail.attendees.all,
            ),
        )
        EventDetailUiState.Unavailable -> EventEditScreenState.Unavailable
    }

    LaunchedEffect(occurrenceId) {
        viewModel.setOccurrenceId(occurrenceId)
    }

    EventEditScreen(
        uiState = uiState,
        goBack = goBack,
        modifier = modifier,
        sharedTransitionScope = LocalSharedTransitionScope.current,
        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
    )
}

@Composable
private fun EventEditScreen(
    uiState: EventEditScreenState,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    Scaffold(
        topBar = { TopAppBar(title = {}) },
    ) { contentPadding ->
        when (uiState) {
            // Coming from the detail screen, the shared flow is already warm, so this is only hit when entering edit directly or
            // recreating the activity, but it still will be loaded from disk which is fast enough.
            EventEditScreenState.Loading -> Unit
            is EventEditScreenState.Success -> EventForm(
                state = uiState.eventFormState,
                onAttendeesClick = { /*TODO[eventForm]*/ },
                modifier = modifier,
                contentPadding = contentPadding + Dimens.EventDetailScreensHorizontalPadding,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
            EventEditScreenState.Unavailable -> LaunchedEffect(Unit) { goBack() }
        }
    }
}

private sealed interface EventEditScreenState {
    object Loading : EventEditScreenState
    data class Success(val eventFormState: EventFormState) : EventEditScreenState
    object Unavailable : EventEditScreenState
}

@Preview
@Composable
private fun Preview() {
    CalendarTheme {
        Surface {
            EventEditScreen(
                uiState = EventEditScreenState.Success(
                    rememberSaveableEventFormState(
                        calendars = listOf(previewEventDetailCalendar),
                        initialCalendar = previewEventDetailCalendar,
                        initialText = "Meeting on how to find funny preview titles",
                    ),
                ),
                goBack = {},
            )
        }
    }
}
