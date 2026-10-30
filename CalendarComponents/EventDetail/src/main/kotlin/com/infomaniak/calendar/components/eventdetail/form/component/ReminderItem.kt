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
package com.infomaniak.calendar.components.eventdetail.form.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIcon
import com.infomaniak.calendar.components.eventdetail.detail.component.notificationTimeText
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.NotificationTime
import com.infomaniak.core.common.R as RCommon
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Cross
import kotlin.time.Duration.Companion.minutes

@Composable
internal fun ReminderItem(notification: EventDetailUi.Notification, onRemoveClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier,
        leadingContent = { ItemIcon(notification.type.icon) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Margin.Mini)) {
                Text(notificationTimeText(notification.time), style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = onRemoveClick) {
                    Icon(EsdsIcons.Cross.Small, contentDescription = stringResource(RCommon.string.deleteButton))
                }
            }
        },
        content = { Text(stringResource(notification.type.label)) },
    )
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            ReminderItem(
                notification = EventDetailUi.Notification(
                    type = EventDetailUi.Notification.Type.Push,
                    time = NotificationTime.Offset((-15).minutes),
                ),
                onRemoveClick = {},
            )
        }
    }
}
