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
package com.infomaniak.calendar.components.event

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.event.component.cardStripes
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.preview.LocalEventColorsUiFactory
import com.infomaniak.calendar.components.foundation.utils.TimeFormatter.formatHours
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.margin.Margin
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Composable
fun EventItem(event: EventUi, modifier: Modifier = Modifier) {
    EventItem(
        start = event.start,
        end = event.end,
        title = event.title,
        status = event.toEventItemStatus(),
        trailingIcons = event.toEventIcons(),
        isAllDay = event.isAllDay,
        modifier = modifier,
    )
}

@Composable
internal fun EventItem(
    start: Instant,
    end: Instant,
    title: String,
    status: EventItemStatus,
    trailingIcons: Set<EventIcons>,
    isAllDay: Boolean,
    modifier: Modifier = Modifier,
) {
    EventItemCard(status, modifier = modifier) {
        if (isAllDay) {
            AllDayContent(title)
        } else {
            PartialDayContent(start, end, title, trailingIcons, status.textDecoration)
        }
    }
}

@Composable
private fun EventItemCard(status: EventItemStatus, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = status.cardColors(),
        border = status.cardBorder(),
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .cardStripes(status)
                .padding(Margin.Mini),
            content = content,
        )
    }
}

@Composable
private fun AllDayContent(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style = MaterialTheme.typography.bodySmallEmphasized,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
private fun PartialDayContent(
    start: Instant,
    end: Instant,
    title: String,
    trailingIcons: Set<EventIcons>,
    textDecoration: TextDecoration?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Margin.Micro),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                "${start.formatHours()} - ${end.formatHours()}",
                style = MaterialTheme.typography.labelSmall,
                textDecoration = textDecoration,
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmallEmphasized,
                fontWeight = FontWeight.Medium,
                textDecoration = textDecoration,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (trailingIcons.isNotEmpty()) TrailingIcons(trailingIcons)
    }

    EventSizeSpacer(end - start)
}

@Composable
private fun TrailingIcons(trailingIcons: Set<EventIcons>) {
    Row(horizontalArrangement = Arrangement.spacedBy(Margin.Micro)) {
        EventIcons.entries.forEach {
            if (it in trailingIcons) it.TrailingIcon()
        }
    }
}

private fun EventUi.toEventIcons(): Set<EventIcons> = buildSet {
    if (location != null) add(EventIcons.Location)
    if (attendees.all.isNotEmpty()) add(EventIcons.Attendees)
    // TODO: Detect kMeet links
}

enum class EventIcons(
    @DrawableRes private val icon: Int,
    @StringRes private val contentDescription: Int,
) {
    Location(R.drawable.ic_map_pin, R.string.contentDescriptionHasLocation),
    Kmeet(R.drawable.ic_product_kmeet, R.string.contentDescriptionHasKMeetLink),
    Attendees(R.drawable.ic_users_stacked, R.string.contentDescriptionHasAttendees);

    @Composable
    internal fun TrailingIcon(modifier: Modifier = Modifier) {
        Icon(painterResource(icon), stringResource(contentDescription), modifier = modifier.size(16.dp))
    }
}

@Preview
@Composable
private fun Preview() {
    @Composable
    fun EventItemForStatus(
        status: EventItemStatus,
        title: String = "Event title",
        isAllDay: Boolean = false,
        modifier: Modifier = Modifier,
    ) {
        EventItem(
            start = Clock.System.now(),
            end = Clock.System.now() + 1.hours,
            isAllDay = isAllDay,
            title = title,
            status = status,
            trailingIcons = EventIcons.entries.toSet(),
            modifier = modifier,
        )
    }

    MaterialTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val eventColors = LocalEventColorsUiFactory.current.create(0x0)

                EventItemForStatus(EventItemStatus.Default(eventColors), isAllDay = true, modifier = Modifier.fillMaxWidth())
                EventItemForStatus(
                    status = EventItemStatus.Default(eventColors),
                    title = "How to not get fired. An important guide on how to navigate the workspace",
                )
                EventItemForStatus(EventItemStatus.Maybe(eventColors))
                EventItemForStatus(EventItemStatus.Declined(eventColors))
                EventItemForStatus(EventItemStatus.Pending(eventColors))
            }
        }
    }
}
