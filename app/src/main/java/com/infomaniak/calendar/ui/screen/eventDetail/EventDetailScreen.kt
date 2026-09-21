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

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.eventdetail.detail.EventDetail
import com.infomaniak.calendar.components.eventdetail.detail.EventDetailTitle
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.common.extensions.safeStartActivity
import com.infomaniak.core.ui.compose.basics.rememberClipboardCopyManager
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import kotlinx.datetime.TimeZone
import kotlin.time.Instant
import com.infomaniak.core.common.R as RCommon

@Composable
fun EventDetailScreen(
    occurrenceId: OccurrenceId,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventDetailViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.eventDetailUi.collectAsStateWithLifecycle(initialValue = EventDetailUiState.Loading)

    LaunchedEffect(occurrenceId) {
        viewModel.setOccurrenceId(occurrenceId)
    }

    EventDetailScreen(
        uiState = { uiState },
        onBack = onBack,
        onLocationClick = { location -> openLocationInMapApp(context, location) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EventDetailScreen(
    uiState: () -> EventDetailUiState,
    onBack: () -> Unit,
    onLocationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        canScroll = { scrollState.canScrollForward || scrollState.canScrollBackward },
    )
    val clipboardManager = rememberClipboardCopyManager()

    Scaffold(
        topBar = {
            TwoRowsTopAppBar(
                title = { _ ->
                    val eventDetail = (uiState() as? EventDetailUiState.Success)?.eventDetail
                    if (eventDetail != null) {
                        EventDetailTitle(eventColor = eventDetail.eventColor, title = eventDetail.title)
                    }
                },
                navigationIcon = { TopAppBarButtons.BackButton(onClick = onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { scaffoldContentPadding ->
        when (val state = uiState()) {
            EventDetailUiState.Loading -> Unit // Loaded locally, always fast, no need for a specific progress indicator UI
            is EventDetailUiState.Success -> {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    val copyFeedbackMessage = stringResource(RCommon.string.linkCopied)

                    EventDetail(
                        eventDetail = state.eventDetail,
                        onJoinKMeet = { /*TODO[eventDetail]*/ },
                        onCopyKMeet = { state.eventDetail.kMeetUrl?.let { clipboardManager.copy(it, copyFeedbackMessage) } },
                        onLocationClick = { state.eventDetail.location?.let { onLocationClick(it) } },
                        onRoomClick = { /*TODO[eventDetail]*/ },
                        contentPadding = scaffoldContentPadding,
                    )
                }
            }
            EventDetailUiState.Deleted -> LaunchedEffect(Unit) { onBack() }
        }
    }
}

private fun openLocationInMapApp(context: Context, location: String) {
    val geoUri = "geo:0,0?q=${Uri.encode(location)}".toUri()
    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)

    context.safeStartActivity(mapIntent)
}

@Preview
@Composable
private fun Preview() {
    val previewEventDetail = EventDetailUi(
        eventColor = Color.Red,
        calendarColor = Color.Blue,
        calendarName = "Vacation",
        title = "Event title",
        start = EventDetailTiming.Precise(Instant.parse("2026-05-20T08:00:00Z"), TimeZone.of("Europe/Paris")),
        end = EventDetailTiming.Precise(Instant.parse("2026-05-20T09:00:00Z"), TimeZone.of("Europe/Paris")),
        isAllDay = false,
        attendees = Attendees(all = emptyList(), me = null),
        kMeetUrl = null,
        location = "Salle Tokyo",
        room = null,
        urlLink = null,
        description = "Description",
        files = emptyList(),
        notifications = emptyList(),
        isOccupied = true,
        classification = EventDetailUi.Classification.Public,
    )

    CalendarThemeForPreview {
        Surface {
            EventDetailScreen(uiState = { EventDetailUiState.Success(previewEventDetail) }, onBack = {}, onLocationClick = {})
        }
    }
}
