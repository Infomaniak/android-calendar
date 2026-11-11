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
package com.infomaniak.calendar.components.eventdetail.detail

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.AttachmentFiles
import com.infomaniak.calendar.components.eventdetail.component.AttendeesButton
import com.infomaniak.calendar.components.eventdetail.component.Calendar
import com.infomaniak.calendar.components.eventdetail.component.ClassificationStatus
import com.infomaniak.calendar.components.eventdetail.component.DateAndTime
import com.infomaniak.calendar.components.eventdetail.component.KMeetButton
import com.infomaniak.calendar.components.eventdetail.component.OccupiedStatus
import com.infomaniak.calendar.components.eventdetail.component.Recurrence
import com.infomaniak.calendar.components.eventdetail.component.Section
import com.infomaniak.calendar.components.eventdetail.component.Title
import com.infomaniak.calendar.components.eventdetail.component.item
import com.infomaniak.calendar.components.eventdetail.detail.component.DescriptionCollapsibleButton
import com.infomaniak.calendar.components.eventdetail.detail.component.LocationButton
import com.infomaniak.calendar.components.eventdetail.detail.component.Notifications
import com.infomaniak.calendar.components.eventdetail.detail.component.RoomButton
import com.infomaniak.calendar.components.eventdetail.models.EventDetailRecurrence
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.modifier.EventSharedElement
import com.infomaniak.calendar.components.eventdetail.modifier.ProvideEventSharedTransition
import com.infomaniak.calendar.components.eventdetail.modifier.eventSharedBounds
import com.infomaniak.calendar.components.eventdetail.modifier.eventSharedElement
import com.infomaniak.calendar.components.eventdetail.preview.previewEventDetailUi
import com.infomaniak.designsystem.core.theme.EsdsTheme

/**
 * [sharedTransitionScope] and [animatedVisibilityScope] animate associated components between detail and editing.
 */
@Composable
fun EventDetail(
    eventDetail: EventDetailUi,
    onJoinKMeet: () -> Unit,
    onCopyKMeet: () -> Unit,
    onLocationClick: () -> Unit,
    goToEventAttendees: () -> Unit,
    onRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) = ProvideEventSharedTransition(sharedTransitionScope, animatedVisibilityScope) {
    Column(modifier = modifier.padding(contentPadding)) {
        with(eventDetail) {
            Section {
                item {
                    Title(dotColor = eventColor, title = title, shapes = it)
                }
            }

            Section {
                item {
                    DateAndTime(start, end, isAllDay, shapes = it)
                }

                if (recurrence is EventDetailRecurrence.Recurrence) {
                    item {
                        Recurrence(recurrence.getText, shapes = it)
                    }
                }
            }

            Section {
                if (attendees.all.isNotEmpty()) {
                    item {
                        AttendeesButton(
                            shapes = it,
                            attendees = { attendees.all },
                            onClick = goToEventAttendees,
                            modifier = Modifier.eventSharedElement(EventSharedElement.Attendees),
                        )
                    }
                }

                if (kMeetUrl?.isNotBlank() == true) {
                    item {
                        KMeetButton(
                            shapes = it,
                            onJoin = onJoinKMeet,
                            onCopy = onCopyKMeet,
                            modifier = Modifier.eventSharedBounds(EventSharedElement.KMeet),
                        )
                    }
                }

                if (location?.isNotBlank() == true) {
                    item {
                        LocationButton(location = location, shapes = it, onClick = onLocationClick)
                    }
                }

                if (room != null) {
                    item {
                        RoomButton(room = room, shapes = it, onClick = onRoomClick)
                    }
                }
            }

            if (description?.isNotBlank() == true || files.isNotEmpty()) {
                Section {
                    if (description?.isNotBlank() == true) {
                        item {
                            DescriptionCollapsibleButton(description = description, shapes = it)
                        }
                    }

                    item {
                        AttachmentFiles(files, onFileClick = { /*TODO[eventDetail]*/ }, shapes = it)
                    }

                }
            }

            if (notifications.isNotEmpty()) {
                Section {
                    item {
                        Notifications(
                            notifications = notifications,
                            onNotificationClick = { /*TODO[eventDetail]*/ },
                            shapes = it,
                        )
                    }
                }
            }

            Section {
                item {
                    OccupiedStatus(
                        isOccupied,
                        modifier = Modifier.eventSharedBounds(EventSharedElement.OccupiedStatus),
                        shapes = it,
                    )
                }
                item {
                    ClassificationStatus(
                        classification = classification,
                        modifier = Modifier.eventSharedBounds(EventSharedElement.ClassificationStatus),
                        shapes = it,
                    )
                }

                item {
                    Calendar(
                        calendar.color,
                        calendar.name,
                        shapes = it,
                        Modifier.eventSharedBounds(EventSharedElement.Calendar),
                    )
                }
            }
        }
    }
}

@Preview(heightDp = 1200)
@Composable
private fun PreviewEventDetail() {
    MaterialTheme {
        Surface {
            Scaffold {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    EventDetail(
                        eventDetail = previewEventDetailUi,
                        onJoinKMeet = {},
                        onCopyKMeet = {},
                        onLocationClick = {},
                        onRoomClick = {},
                        goToEventAttendees = {},
                        contentPadding = PaddingValues(horizontal = EsdsTheme.spacing.lg) + it,
                    )
                }
            }
        }
    }
}
