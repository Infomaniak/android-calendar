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
package com.infomaniak.calendar.components.eventdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infomaniak.calendar.components.eventdetail.component.DateAndTime
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.utils.fromAttendee
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.ui.compose.basics.onlyHorizontal
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

private val LIST_ITEM_HORIZONTAL_PADDING = Margin.Medium

@Composable
fun EventDetail(
    eventDetail: EventDetailUi,
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

            if (eventDetail.attendees.all.isNotEmpty()) {
                Divider(modifier = Modifier.padding(horizontalContentPadding))
                AttendeesButton(eventDetail.attendees.all, onClick = {}, contentPadding = horizontalContentPadding)
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
                    .size(20.dp)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AttendeesButton(
    attendees: List<AttendeeUi>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    ListItem(
        contentPadding = contentPadding + PaddingValues(horizontal = LIST_ITEM_HORIZONTAL_PADDING),
        onClick = onClick,
        shapes = ListItemDefaults.RectangleShapes,
        leadingContent = { Icon(painterResource(R.drawable.ic_users_stacked), contentDescription = null) },
        content = { Text(text = pluralStringResource(R.plurals.attendeesCount, attendees.size, attendees.size)) },
        supportingContent = {
            val acceptedCount = attendees.count { it.status == ParticipationStatus.Accepted }
            Text(text = pluralStringResource(R.plurals.attendeesAcceptedCount, acceptedCount, acceptedCount))
        },
        trailingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(Margin.Mini), verticalAlignment = Alignment.CenterVertically) {
                StackedAvatars(attendees)
                Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null)
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun StackedAvatars(attendees: List<AttendeeUi>) {
    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp), verticalAlignment = Alignment.CenterVertically) {
        val border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface)
        val size = 32.dp

        attendees.take(3).forEach { attendee ->
            Avatar(AvatarType.fromAttendee(attendee), Modifier.size(size), border)
        }
        if (attendees.size > 3) ExtraCountIndicator(attendees.size, border, Modifier.size(size))
    }
}

@Composable
private fun ExtraCountIndicator(extraCount: Int, border: BorderStroke, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .border(border, CircleShape),
    ) {
        val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
        val textStyle = MaterialTheme.typography.labelLargeEmphasized

        BasicText(
            text = "+${extraCount - 3}",
            style = textStyle,
            color = { onPrimaryContainer },
            autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = textStyle.fontSize),
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val ListItemDefaults.RectangleShapes
    @Composable
    get() = shapes(
        shape = RectangleShape,
        selectedShape = RectangleShape,
        pressedShape = RectangleShape,
        focusedShape = RectangleShape,
        hoveredShape = RectangleShape,
        draggedShape = RectangleShape,
    )

@Composable
private fun Divider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.padding(LIST_ITEM_HORIZONTAL_PADDING))
}

@Preview
@Composable
private fun PreviewEventDetail() {
    val eventDetail = EventDetailUi(
        eventColor = Color.Red,
        calendarColor = Color.Blue,
        title = "Event Title",
        start = EventDetailTiming.Precise(Instant.parse("2026-05-20T08:00:00Z"), TimeZone.of("Europe/Paris")),
        end = EventDetailTiming.Precise(Instant.parse("2026-05-20T09:00:00Z"), TimeZone.of("Europe/Paris")),
        isAllDay = false,
        attendees = Attendees(
            all = listOf(
                AttendeeUi(email = "alice@example.com", displayName = "Alice Martin", status = ParticipationStatus.Accepted),
                AttendeeUi(email = "bob@example.com", displayName = "Bob Durand", status = ParticipationStatus.Tentative),
                AttendeeUi(email = "carla@example.com", displayName = "Carla Dupont", status = ParticipationStatus.Declined),
                AttendeeUi(email = "denis@example.com", displayName = "Denis Bernard", status = ParticipationStatus.NeedsAction),
            ),
            me = null,
        ),
        kMeetUrl = null,
        location = "Location",
        room = null,
        urlLink = null,
        description = "Description",
        files = emptyList(),
        notifications = emptyList(),
    )

    MaterialTheme {
        Surface {
            EventDetail(eventDetail, contentPadding = PaddingValues(horizontal = Margin.Small))
        }
    }
}
