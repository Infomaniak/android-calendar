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
package com.infomaniak.calendar.components.eventdetail.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi.Notification.NotificationTime
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDateTime
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Composable
internal fun Notifications(
    notifications: List<EventDetailUi.Notification>,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    notifications.forEach { notification ->
        val timeText = notificationTimeText(notification.time)

        ClickableItem(
            text = stringResource(notification.type.label),
            leadingIconRes = notification.type.icon,
            onClick = { onNotificationClick() },
            contentPadding = contentPadding,
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Margin.Mini)) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(0.5f),
                    )
                    Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null)
                }
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun notificationTimeText(time: NotificationTime): String {
    val timeZone = TimeZone.currentSystemDefault()
    val currentYear = Clock.today(timeZone).year

    return when (time) {
        is NotificationTime.Offset -> time.duration.formatDurationOffset()
        is NotificationTime.Absolute -> time.instant.formatDateTime(timeZone = timeZone, currentYear = currentYear)
    }
}

@Composable
fun Duration.formatDurationOffset(): String {
    val isBefore = isNegative()
    val absoluteDuration = absoluteValue
    if (absoluteDuration.inWholeSeconds == 0L) {
        return stringResource(R.string.notificationTimeAtStart)
    }
    val totalMinutes = absoluteDuration.inWholeMinutes
    val weeks = totalMinutes / (7 * 24 * 60)
    val days = (totalMinutes % (7 * 24 * 60)) / (24 * 60)
    val hours = (totalMinutes % (24 * 60)) / 60
    val minutes = totalMinutes % 60

    val parts = buildList {
        if (weeks > 0) add(pluralStringResource(R.plurals.weekAmount, weeks.toInt(), weeks.toInt()))
        if (days > 0) add(pluralStringResource(R.plurals.dayAmount, days.toInt(), days.toInt()))
        if (hours > 0) add(pluralStringResource(R.plurals.hourAmount, hours.toInt(), hours.toInt()))
        if (minutes > 0) add(pluralStringResource(R.plurals.minuteAmount, minutes.toInt(), minutes.toInt()))
    }

    val durationText = parts.joinToString(", ")
    return if (isBefore) {
        stringResource(R.string.notificationTimeBefore, durationText)
    } else {
        stringResource(R.string.notificationTimeAfter, durationText)
    }
}

@Preview(name = "Absolute notification")
@Composable
private fun PreviewAbsoluteNotification() {
    MaterialTheme {
        Surface {
            Notifications(
                notifications = listOf(
                    EventDetailUi.Notification(
                        EventDetailUi.Notification.Type.Email,
                        NotificationTime.Absolute(Instant.parse("2026-05-20T07:00:00Z")),
                    ),
                ),
                onNotificationClick = {},
            )
        }
    }
}

@Preview(name = "Absolute notification next year")
@Composable
private fun PreviewAbsoluteNotificationNextYear() {
    MaterialTheme {
        Surface {
            Notifications(
                notifications = listOf(
                    EventDetailUi.Notification(
                        EventDetailUi.Notification.Type.Email,
                        NotificationTime.Absolute(Instant.parse("2027-05-20T07:00:00Z")),
                    ),
                ),
                onNotificationClick = {},
            )
        }
    }
}

@Preview(name = "Offset notification")
@Composable
private fun PreviewOffsetNotification() {
    MaterialTheme {
        Surface {
            Notifications(
                notifications = listOf(
                    EventDetailUi.Notification(
                        EventDetailUi.Notification.Type.Push,
                        NotificationTime.Offset((-90).minutes),
                    ),
                ),
                onNotificationClick = {},
            )
        }
    }
}

@Preview(name = "Long Offset notification")
@Composable
private fun PreviewLongOffsetNotification() {
    MaterialTheme {
        Surface {
            Notifications(
                notifications = listOf(
                    EventDetailUi.Notification(
                        EventDetailUi.Notification.Type.Push,
                        NotificationTime.Offset(90.days + 30.minutes),
                    ),
                ),
                onNotificationClick = {},
            )
        }
    }
}

@Preview(name = "Offset notification at start")
@Composable
private fun PreviewOffsetNotificationAtStart() {
    MaterialTheme {
        Surface {
            Notifications(
                notifications = listOf(
                    EventDetailUi.Notification(
                        EventDetailUi.Notification.Type.Push,
                        NotificationTime.Offset((0).seconds),
                    ),
                ),
                onNotificationClick = {},
            )
        }
    }
}
