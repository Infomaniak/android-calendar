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
package com.infomaniak.calendar.components.attendeessearch.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.utils.fromAttendee
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.core.theme.EsdsTheme.extendedColorScheme

@Composable
fun EventAttendee(attendee: AttendeeUi, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(start = EsdsTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarType = AvatarType.fromAttendee(attendee),
            modifier = Modifier
                .padding(start = EsdsTheme.spacing.lg)
                .size(40.dp),
        )
        ListItem(
            modifier = Modifier,
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            overlineContent = {
                AttendeeParticipationStatus(attendee.status, attendee.isOrganizer)
            },
            supportingContent = if (attendee.displayName.isNullOrEmpty()) {
                null
            } else {
                {
                    Text(
                        text = attendee.email,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            content = {
                Column {
                    Text(
                        text = attendee.displayName ?: attendee.email,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                    )
                }
            },
        )
    }
}

@Composable
fun AttendeeParticipationStatus(status: ParticipationStatus, isOrganizer: Boolean, modifier: Modifier = Modifier) {
    return when (status) {
        ParticipationStatus.Accepted -> {
            ParticipationStatusText(
                text = stringResource(R.string.statusAcceptedLabel),
                color = MaterialTheme.extendedColorScheme.success,
                isOrganizer = isOrganizer,
                modifier = modifier,
            )
        }
        ParticipationStatus.Declined -> {
            ParticipationStatusText(
                text = stringResource(R.string.statusDeclinedLabel),
                color = MaterialTheme.colorScheme.error,
                isOrganizer = isOrganizer,
                modifier = modifier,
            )
        }
        ParticipationStatus.Tentative -> {
            ParticipationStatusText(
                text = stringResource(R.string.statusTentativeLabel),
                color = MaterialTheme.extendedColorScheme.warning,
                isOrganizer = isOrganizer,
                modifier = modifier,
            )
        }
        ParticipationStatus.NeedsAction -> {
            ParticipationStatusText(
                text = stringResource(R.string.statusNeedsActionLabel),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                isOrganizer = isOrganizer,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun ParticipationStatusText(text: String, color: Color, isOrganizer: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(
            text = text,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
            overflow = TextOverflow.Ellipsis,
            color = color,
        )
        if (isOrganizer) {
            Text(
                text = " · ${stringResource(R.string.sectionOrganizerHeader)}",
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewAttendeeAccepted() {
    Surface() {
        EventAttendee(AttendeeUi("alice@example.com", "Alice", ParticipationStatus.Accepted, true))
    }
}

@Preview
@Composable
private fun PreviewAttendeeDeclined() {
    Surface() {
        EventAttendee(AttendeeUi("alice@example.com", null, ParticipationStatus.Declined))
    }
}

@Preview
@Composable
private fun PreviewAttendeeMaybe() {
    Surface() {
        EventAttendee(AttendeeUi("alice@example.com", "Alice", ParticipationStatus.Tentative))
    }
}

@Preview
@Composable
private fun PreviewAttendeePending() {
    Surface() {
        EventAttendee(AttendeeUi("alice@example.com", "Alice", ParticipationStatus.NeedsAction))
    }
}

