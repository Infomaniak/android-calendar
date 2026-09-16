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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.utils.fromAttendee
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.designsystem.core.theme.EsdsTheme.extendedColorScheme

@Composable
fun EventAttendee(attendee: AttendeeUi, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier.fillMaxWidth(),
        headlineContent = {
            Column() {
                // had to add this here and not overline content to center the avatar vertically
                AttendeeParticipationStatus(attendee.status)
                Text(
                    text = attendee.displayName.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

        },
        supportingContent = {
            Text(
                text = attendee.email,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingContent = {
            Avatar(
                avatarType = AvatarType.fromAttendee(attendee),
                modifier = Modifier.size(40.dp),
            )
        },
    )
}

@Composable
fun AttendeeParticipationStatus(status: ParticipationStatus, modifier: Modifier = Modifier) {
    return when (status) {
        ParticipationStatus.Accepted -> {
            ParticipationStatusText(
                text = "Accepted",
                color = MaterialTheme.extendedColorScheme.success,
            )
        }
        ParticipationStatus.Declined -> {
            ParticipationStatusText(
                text = "Declined",
                color = MaterialTheme.colorScheme.error, //TODO: check correct color
            )
        }
        ParticipationStatus.Tentative -> {
            ParticipationStatusText(
                text = "Maybe",
                color = MaterialTheme.extendedColorScheme.datavizYellow,  //TODO: check correct color
            )
        }
        ParticipationStatus.NeedsAction -> {
            ParticipationStatusText(
                text = "Pending",
                color = MaterialTheme.extendedColorScheme.warning,
            )
        }
    }
}

@Composable
fun ParticipationStatusText(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        maxLines = 1,
        style = MaterialTheme.typography.bodyMedium,
        overflow = TextOverflow.Ellipsis,
        color = color,
        fontWeight = FontWeight.Medium,
    )
}

@Preview
@Composable
private fun PreviewAttendeeAccepted() {
    EventAttendee(AttendeeUi("alice@example.com", "Alice", ParticipationStatus.Accepted))
}

@Preview
@Composable
private fun PreviewAttendeeDeclined() {
    EventAttendee(AttendeeUi("alice@example.com", "Alice", ParticipationStatus.Declined))
}

@Preview
@Composable
private fun PreviewAttendeeMaybe() {
    EventAttendee(AttendeeUi("alice@example.com", "Alice", ParticipationStatus.Tentative))
}

@Preview
@Composable
private fun PreviewAttendeePending() {
    EventAttendee(AttendeeUi("alice@example.com", "Alice", ParticipationStatus.NeedsAction))
}

