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
package com.infomaniak.calendar.ui.screen.eventDetail.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.infomaniak.calendar.components.eventdetail.detail.EventDetail
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.modifier.LocalSharedTransitionScope
import com.infomaniak.calendar.ui.screen.eventDetail.EventDetailUiState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.ui.theme.Dimens
import com.infomaniak.core.common.extensions.safeStartActivity
import com.infomaniak.core.ui.compose.basics.rememberClipboardCopyManager
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import kotlinx.datetime.TimeZone
import kotlin.time.Instant
import com.infomaniak.core.common.R as RCommon

@Composable
fun EventDetailScreen(
    occurrenceId: OccurrenceId,
    goBack: () -> Unit,
    goToEdit: () -> Unit,
    goToEventAttendees: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventDetailViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.eventDetailUi.collectAsStateWithLifecycle()

    LaunchedEffect(occurrenceId) {
        viewModel.setOccurrenceId(occurrenceId)
    }

    EventDetailScreen(
        uiState = { uiState },
        sharedTransitionScope = LocalSharedTransitionScope.current,
        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
        goBack = goBack,
        goToEdit = goToEdit,
        onLocationClick = { location -> openLocationInMapApp(context, location) },
        goToEventAttendees = goToEventAttendees,
        modifier = modifier,
    )
}

@Composable
private fun EventDetailScreen(
    uiState: () -> EventDetailUiState,
    goBack: () -> Unit,
    goToEdit: () -> Unit,
    goToEventAttendees: () -> Unit,
    onLocationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val clipboardManager = rememberClipboardCopyManager()
    val state = uiState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { TopAppBarButtons.BackButton(onClick = goBack) },
                title = {},
                actions = {
                    val canEdit = (state as? EventDetailUiState.Success)?.eventDetail?.canEdit == true
                    TopAppBarButtons.EditButton(onClick = goToEdit, enabled = canEdit)
                },
            )
        },
        modifier = modifier,
    ) { scaffoldContentPadding ->
        when (state) {
            EventDetailUiState.Loading -> Unit // Loaded locally, always fast, no need for a specific progress indicator UI
            is EventDetailUiState.Success -> {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val copyFeedbackMessage = stringResource(RCommon.string.linkCopied)

                    EventDetail(
                        eventDetail = state.eventDetail,
                        onJoinKMeet = { /*TODO[eventDetail]*/ },
                        onCopyKMeet = { state.eventDetail.kMeetUrl?.let { clipboardManager.copy(it, copyFeedbackMessage) } },
                        onLocationClick = { state.eventDetail.location?.let { onLocationClick(it) } },
                        onRoomClick = { /*TODO[eventDetail]*/ },
                        goToEventAttendees = { goToEventAttendees() },
                        contentPadding = scaffoldContentPadding + Dimens.EventDetailScreensHorizontalPadding,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            }
            EventDetailUiState.Unavailable -> LaunchedEffect(Unit) { goBack() }
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
        canEdit = true,
    )

    CalendarThemeForPreview {
        Surface {
            EventDetailScreen(
                uiState = { EventDetailUiState.Success(previewEventDetail) },
                goBack = {},
                goToEdit = {},
                goToEventAttendees = {},
                onLocationClick = {},
            )
        }
    }
}
