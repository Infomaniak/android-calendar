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
package com.infomaniak.calendar.ui.screen.eventDetail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.eventdetail.EventDetail
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.TimeZone
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

private val LOADING_INDICATOR_DELAY = 600.milliseconds

@Composable
fun EventDetailScreen(
    masterEventId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventDetailViewModel = viewModel(),
) {
    val eventDetailFlow = remember(masterEventId) { viewModel.observeEventDetail(masterEventId) }
    val uiState by eventDetailFlow.collectAsStateWithLifecycle(initialValue = EventDetailUiState.Loading)

    EventDetailScreen(uiState = { uiState }, onBack = onBack, modifier = modifier)
}

@Composable
private fun EventDetailScreen(uiState: () -> EventDetailUiState, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { TopAppBar(navigationIcon = { TopAppBarButtons.BackButton(onClick = onBack) }, title = {}) },
        modifier = modifier,
    ) { scaffoldContentPadding ->
        when (val state = uiState()) {
            EventDetailUiState.Loading -> Unit // Loaded locally, always fast, no need for a specific progress indicator UI
            is EventDetailUiState.Success -> {
                EventDetail(
                    eventDetail = state.eventDetail,
                    contentPadding = scaffoldContentPadding + PaddingValues(horizontal = Margin.Small),
                )
            }
            EventDetailUiState.Deleted -> LaunchedEffect(Unit) { onBack() }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val previewEventDetail = EventDetailUi(
        eventColor = Color.Red,
        calendarColor = Color.Blue,
        title = "Event title",
        start = EventDetailTiming.Precised(Instant.parse("2026-05-20T08:00:00Z"), TimeZone.of("Europe/Paris")),
        end = EventDetailTiming.Precised(Instant.parse("2026-05-20T09:00:00Z"), TimeZone.of("Europe/Paris")),
        isAllDay = false,
        attendees = Attendees(all = emptyList(), me = null),
        kMeetUrl = null,
        location = "Salle Tokyo",
        room = null,
        urlLink = null,
        description = "Description",
        files = emptyList(),
        notifications = emptyList(),
    )

    CalendarThemeForPreview {
        Surface {
            EventDetailScreen(uiState = { EventDetailUiState.Success(previewEventDetail) }, onBack = {})
        }
    }
}
