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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.utils.toAttendeeUi
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.multiplatform_calendar.core.domain.model.event.Attendee
import com.infomaniak.multiplatform_calendar.core.domain.model.event.AttendeeRole
import com.infomaniak.multiplatform_calendar.core.domain.model.event.ParticipationStatus

@Composable
fun EventAttendeesScreen(
    eventId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventDetailViewModel = viewModel(),
) {
    val attendees by viewModel.eventAttendees.collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(Unit) {
        viewModel.setEventId(eventId)
    }

    EventAttendeesScreen({ attendees }, onBack, modifier)
}

@Composable
fun EventAttendeesScreen(attendees: () -> List<Attendee>?, onBack: () -> Unit, modifier: Modifier = Modifier) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val allAttendees = attendees().orEmpty().map(Attendee::toAttendeeUi)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { TopAppBarButtons.BackButton(onClick = onBack) },
                title = { Text(text = stringResource(R.string.attendeesTitle)) },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // TODO: add search contacts functionality
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .padding(horizontal = Margin.Small, vertical = Margin.Small)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(color = MaterialTheme.colorScheme.surfaceContainerHigh),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_magnifying_glass),
                        contentDescription = stringResource(R.string.contentDescriptionSearch),
                    )
                },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.searchForAttendees)) },
            )

            // TODO: do a proper empty state view
            if (allAttendees.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No attendees found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = EsdsTheme.spacing.md),
                ) {
                    items(
                        items = allAttendees,
                        key = { attendee -> attendee.email },
                    ) { attendee ->
                        EventAttendee(attendee = attendee)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun EventAttendeesScreenPreview() {
    val previewAttendees = listOf(
        Attendee(
            email = "alice@example.com",
            displayName = "Alice Johnson",
            status = ParticipationStatus.Accepted,
            role = AttendeeRole.Chair,
            isOrganizer = true,
        ),
        Attendee(
            email = "bob@example.com",
            displayName = "Bob Smith",
            status = ParticipationStatus.Tentative,
            role = AttendeeRole.Optional,
        ),
        Attendee(
            email = "charlie@example.com",
            displayName = "Charlie Brown",
            status = ParticipationStatus.Declined,
            role = AttendeeRole.Optional,
        ),
    )

    CalendarThemeForPreview {
        Surface {
            EventAttendeesScreen(
                attendees = { previewAttendees },
                onBack = {},
            )
        }
    }
}
