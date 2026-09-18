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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.component.AttachmentFiles
import com.infomaniak.calendar.components.eventdetail.detail.component.AttendeesButton
import com.infomaniak.calendar.components.eventdetail.detail.component.Calendar
import com.infomaniak.calendar.components.eventdetail.detail.component.ClassificationStatus
import com.infomaniak.calendar.components.eventdetail.detail.component.DateAndTime
import com.infomaniak.calendar.components.eventdetail.detail.component.DescriptionCollapsibleButton
import com.infomaniak.calendar.components.eventdetail.detail.component.KMeetButton
import com.infomaniak.calendar.components.eventdetail.detail.component.LIST_ITEM_HORIZONTAL_PADDING
import com.infomaniak.calendar.components.eventdetail.detail.component.LocationButton
import com.infomaniak.calendar.components.eventdetail.detail.component.Notifications
import com.infomaniak.calendar.components.eventdetail.detail.component.OccupiedStatus
import com.infomaniak.calendar.components.eventdetail.detail.component.RoomButton
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.previewAttendees
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

@Composable
fun EventDetail(
    eventDetail: EventDetailUi,
    onJoinKMeet: () -> Unit,
    onCopyKMeet: () -> Unit,
    onLocationClick: () -> Unit,
    onRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val horizontalContentPadding = contentPadding.onlyHorizontal()

    Column(
        modifier = modifier.padding(top = contentPadding.calculateTopPadding(), bottom = contentPadding.calculateBottomPadding()),
    ) {
        with(eventDetail) {
            Title(eventColor, title, Modifier.padding(horizontalContentPadding))
            DateAndTime(start, end, isAllDay, Modifier.padding(horizontalContentPadding))

            Section(contentPadding = horizontalContentPadding) {
                if (attendees.all.isNotEmpty()) {
                    AttendeesButton(attendees.all, onClick = {}, contentPadding = horizontalContentPadding)
                }

                if (kMeetUrl?.isNotBlank() == true) {
                    KMeetButton(onJoin = onJoinKMeet, onCopy = onCopyKMeet, modifier = Modifier.padding(horizontalContentPadding))
                }

                if (location?.isNotBlank() == true) {
                    LocationButton(location = location, onClick = onLocationClick, contentPadding = horizontalContentPadding)
                }

                if (room != null) {
                    RoomButton(room = room, onClick = onRoomClick, contentPadding = horizontalContentPadding)
                }
            }

            Section(contentPadding = horizontalContentPadding) {
                if (description?.isNotBlank() == true) {
                    DescriptionCollapsibleButton(description = description, contentPadding = horizontalContentPadding)
                }

                AttachmentFiles(files, onFileClick = { /*TODO[eventDetail]*/ }, contentPadding = horizontalContentPadding)
            }

            Section(contentPadding = horizontalContentPadding) {
                Notifications(
                    notifications,
                    onNotificationClick = { /*TODO[eventDetail]*/ },
                    contentPadding = horizontalContentPadding,
                )
            }

            Section(contentPadding = horizontalContentPadding) {
                OccupiedStatus(isOccupied, modifier = Modifier.padding(horizontalContentPadding))

                if (classification != null) {
                    ClassificationStatus(classification, modifier = Modifier.padding(horizontalContentPadding))
                }

                Calendar(calendarColor, calendarName, modifier = Modifier.padding(horizontalContentPadding))
            }
        }
    }
}

@Composable
private fun Title(color: Color, title: String, modifier: Modifier = Modifier) {
    ListItem(
        headlineContent = { Text(text = title, style = MaterialTheme.typography.titleLargeEmphasized) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(EsdsTheme.icon.sizeMd)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        },
        modifier = modifier,
    )
}

/**
 * Automatically shows or hides divider based on if any content is composed or not. This layout acts like a column.
 */
@Composable
private fun Section(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable () -> Unit,
) {
    Layout(
        contents = listOf({ Divider(modifier = Modifier.padding(contentPadding)) }, content),
        modifier = modifier,
    ) { (dividerMeasurables, contentMeasurables), constraints ->
        if (contentMeasurables.isEmpty()) return@Layout layout(0, 0) {}

        val childConstraints = constraints.copy(minHeight = 0)
        val placeables = (dividerMeasurables + contentMeasurables).map { it.measure(childConstraints) }

        val width = placeables.maxOf { it.width }.coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = placeables.sumOf { it.height }.coerceIn(constraints.minHeight, constraints.maxHeight)

        layout(width, height) {
            var y = 0
            placeables.forEach { placeable ->
                placeable.place(0, y)
                y += placeable.height
            }
        }
    }
}

@Composable
private fun Divider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.padding(LIST_ITEM_HORIZONTAL_PADDING))
}

@Preview(heightDp = 1200)
@Composable
private fun PreviewEventDetail() {
    val eventDetail = EventDetailUi(
        eventColor = Color.Red,
        calendarColor = Color.Blue,
        calendarName = "Vacation",
        title = "Event Title",
        start = EventDetailTiming.Precise(Instant.parse("2026-05-20T08:00:00Z"), TimeZone.of("Europe/Paris")),
        end = EventDetailTiming.Precise(Instant.parse("2026-05-20T09:00:00Z"), TimeZone.of("Europe/Paris")),
        isAllDay = false,
        attendees = Attendees(all = previewAttendees, me = null),
        kMeetUrl = "test url",
        location = "Location",
        room = EventDetailUi.Room("Japan room", 5, 3),
        urlLink = null,
        description = LoremIpsum(30).values.first(),
        files = listOf(
            EventDetailUi.File("1", "How to not get fired.pdf"),
            EventDetailUi.File("2", "Bob.txt"),
            EventDetailUi.File("3", "Next loto results.png"),
        ),
        notifications = listOf(
            EventDetailUi.Notification("1", EventDetailUi.Notification.Type.Email, Instant.parse("2026-05-20T07:00:00Z")),
            EventDetailUi.Notification("2", EventDetailUi.Notification.Type.Push, Instant.parse("2026-05-20T07:30:00Z")),
        ),
        isOccupied = true,
        classification = EventDetailUi.Classification.Public,
    )

    MaterialTheme {
        Surface {
            Scaffold {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    EventDetail(
                        eventDetail = eventDetail,
                        onJoinKMeet = {},
                        onCopyKMeet = {},
                        onLocationClick = {},
                        onRoomClick = {},
                        contentPadding = PaddingValues(horizontal = Margin.Small) + it,
                    )
                }
            }
        }
    }
}
