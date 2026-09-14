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
package com.infomaniak.calendar.components.eventdetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.margin.Margin
import kotlin.time.Instant

@Composable
internal fun Notifications(
    notifications: List<EventDetailUi.Notification>,
    onNotificationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    notifications.forEach { notification ->
        ClickableItem(
            text = stringResource(notification.type.label),
            leadingIconRes = notification.type.icon,
            onClick = { onNotificationClick(notification.id) },
            contentPadding = contentPadding,
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Margin.Mini)) {
                    // TODO[eventDetai]: Format the notification time
                    Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null)
                }
            },
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun PreviewNotifications() {
    MaterialTheme {
        Surface {
            Notifications(
                notifications = listOf(
                    EventDetailUi.Notification("1", EventDetailUi.Notification.Type.Email, Instant.parse("2026-05-20T07:00:00Z")),
                    EventDetailUi.Notification("2", EventDetailUi.Notification.Type.Push, Instant.parse("2026-05-20T07:30:00Z")),
                ),
                onNotificationClick = {},
            )
        }
    }
}
