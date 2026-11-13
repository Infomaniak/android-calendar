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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIcon
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.eventdetail.models.ReminderTime
import com.infomaniak.calendar.components.eventdetail.modifier.reminderSharedBounds
import com.infomaniak.calendar.components.eventdetail.theme.CustomColors
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDateTime
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDurationOffset
import com.infomaniak.core.common.utils.today
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Cross
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import com.infomaniak.core.common.R as RCommon


@Composable
internal fun ReminderItem(
    reminder: EventDetailUi.Reminder,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    SegmentedListItem(
        shapes = shapes,
        colors = CustomColors.EventFormItemColors,
        content = { Text(stringResource(reminder.type.label)) },
        leadingContent = { ItemIcon(reminder.type.icon) },
        contentPadding = contentPadding + ListItemDefaults.ContentPadding,
        trailingContent = {
            Text(
                text = reminderTimeText(reminder.time),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(0.5f),
            )
        },
        modifier = modifier.reminderSharedBounds(reminder),
    )
}

@Composable
internal fun reminderTimeText(time: ReminderTime): String {
    return when (time) {
        is ReminderTime.Offset -> time.duration.formatDurationOffset()
        is ReminderTime.Absolute -> {
            val timeZone = TimeZone.currentSystemDefault()
            val currentYear = Clock.today(timeZone).year
            time.instant.formatDateTime(timeZone = timeZone, currentYear = currentYear)
        }
    }
}


@Composable
internal fun ReminderItemEditable(
    reminder: EventDetailUi.Reminder,
    onRemoveClick: () -> Unit,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        modifier = modifier,
        shapes = shapes,
        colors = CustomColors.EventFormItemColors,
        leadingContent = { ItemIcon(reminder.type.icon) },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md),
            ) {
                Text(reminderTimeText(reminder.time), style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = onRemoveClick) {
                    Icon(EsdsIcons.Cross.Small, contentDescription = stringResource(RCommon.string.deleteButton))
                }
            }
        },
        content = { Text(stringResource(reminder.type.label)) },
    )
}

@Preview
@Composable
private fun PreviewReminderItemEditable() {
    MaterialTheme {
        Surface {
            ReminderItemEditable(
                reminder = EventDetailUi.Reminder(
                    type = EventDetailUi.Reminder.Type.Push,
                    time = ReminderTime.Offset((-15).minutes),
                ),
                onRemoveClick = {},
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
        }
    }
}

@Preview(name = "Absolute reminder")
@Composable
private fun PreviewAbsoluteReminder() {
    MaterialTheme {
        Surface {
            ReminderItem(
                reminder =
                    EventDetailUi.Reminder(
                        EventDetailUi.Reminder.Type.Email,
                        ReminderTime.Absolute(Instant.parse("2026-05-20T07:00:00Z")),
                    ),
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
        }
    }
}

@Preview(name = "Absolute reminder next year")
@Composable
private fun PreviewAbsoluteReminderNextYear() {
    MaterialTheme {
        Surface {
            ReminderItem(
                reminder = EventDetailUi.Reminder(
                    EventDetailUi.Reminder.Type.Email,
                    ReminderTime.Absolute(Instant.parse("2027-05-20T07:00:00Z")),
                ),
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
        }
    }
}

@Preview(name = "Offset reminder")
@Composable
private fun PreviewOffsetReminder() {
    MaterialTheme {
        Surface {
            ReminderItem(
                reminder = EventDetailUi.Reminder(
                    EventDetailUi.Reminder.Type.Push,
                    ReminderTime.Offset((-90).minutes),
                ),
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
        }
    }
}

@Preview(name = "Long Offset reminder")
@Composable
private fun PreviewLongOffsetReminder() {
    MaterialTheme {
        Surface {
            ReminderItem(
                reminder = EventDetailUi.Reminder(
                    EventDetailUi.Reminder.Type.Push,
                    ReminderTime.Offset(90.days + 30.minutes),
                ),
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
        }
    }
}

@Preview(name = "Offset reminder at start")
@Composable
private fun PreviewOffsetReminderAtStart() {
    MaterialTheme {
        Surface {
            ReminderItem(
                reminder = EventDetailUi.Reminder(
                    EventDetailUi.Reminder.Type.Push,
                    ReminderTime.Offset((0).seconds),
                ),
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            )
        }
    }
}
