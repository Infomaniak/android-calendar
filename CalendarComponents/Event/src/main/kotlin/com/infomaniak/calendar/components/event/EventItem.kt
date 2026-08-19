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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.event.component.cardStripes
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.preview.LocalEventColorsUiFactory
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.HourFormatter.formatHours
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Composable
fun EventItem(event: EventUi.Normal, modifier: Modifier = Modifier) {
    EventItem(
        start = event.start,
        end = event.end,
        title = event.title,
        location = event.location,
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
    location: String?,
    status: EventItemStatus,
    trailingIcons: Set<EventIcons>,
    isAllDay: Boolean,
    modifier: Modifier = Modifier,
) {
    EventItemCard(status, modifier = modifier) {
        if (isAllDay) {
            AllDayContent(title)
        } else {
            PartialDayContent(start, end, title, location, trailingIcons, status.textDecoration)
        }
    }
}

@Composable
private fun EventItemCard(status: EventItemStatus, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val cardLine = status.sourceColor() ?: status.cardColors().containerColor

    Card(
        colors = status.cardColors(),
        border = status.cardBorder(),
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min), // makes the line match card height
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(cardLine),
            )
            Column(
                modifier = Modifier
                    .cardStripes(status)
                    .padding(EsdsTheme.spacing.sm),
                content = content,
            )
        }
    }
}

@Composable
private fun AllDayContent(title: String, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xs),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodySmallEmphasized,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier.weight(1f),
        )

        Text(stringResource(R.string.allDayLabel), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PartialDayContent(
    start: Instant,
    end: Instant,
    title: String,
    location: String?,
    trailingIcons: Set<EventIcons>,
    textDecoration: TextDecoration?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.sm),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.twoXs),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                textDecoration = textDecoration,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatDisplayedHour(start, end),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                textDecoration = textDecoration,
            )

            if (location != null) {
                Text(
                    text = location,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = textDecoration,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

        }

        if (trailingIcons.isNotEmpty()) TrailingIcons(trailingIcons)
    }

    EventSizeSpacer(end - start)
}

@Composable
private fun formatDisplayedHour(start: Instant, end: Instant): String = "${start.formatHours()} - ${end.formatHours()}"

@Composable
private fun TrailingIcons(trailingIcons: Set<EventIcons>) {
    Row(horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xs)) {
        EventIcons.entries.forEach {
            if (it in trailingIcons) it.TrailingIcon()
        }
    }
}

fun EventUi.Normal.toEventIcons(): Set<EventIcons> = buildSet {
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
    fun TrailingIcon(modifier: Modifier = Modifier, size: Dp = TrailingIconSize) {
        Icon(painterResource(icon), stringResource(contentDescription), modifier = modifier.size(size))
    }

    companion object {
        /** An icon trails a title, so a card shrinking its title scales its icons down with it. */
        val TrailingIconSize: Dp = 16.dp
    }
}

@Preview
@Composable
private fun Preview() {
    @Composable
    fun EventItemForStatus(
        status: EventItemStatus,
        title: String = "Event title",
        location: String? = null,
        isAllDay: Boolean = false,
        modifier: Modifier = Modifier,
    ) {
        EventItem(
            start = Clock.System.now(),
            end = Clock.System.now() + 1.hours,
            isAllDay = isAllDay,
            title = title,
            location = location,
            status = status,
            trailingIcons = EventIcons.entries.toSet(),
            modifier = modifier,
        )
    }

    MaterialTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.sm)) {
                val eventColors = LocalEventColorsUiFactory.current.create(0x0)

                EventItemForStatus(EventItemStatus.Default(eventColors), isAllDay = true, modifier = Modifier.fillMaxWidth())
                EventItemForStatus(
                    status = EventItemStatus.Default(eventColors),
                    title = "How to not get fired. An important guide on how to navigate the workspace",
                    location = "Salle Tokyo",
                )
                EventItemForStatus(EventItemStatus.Maybe(eventColors))
                EventItemForStatus(EventItemStatus.Declined(eventColors))
                EventItemForStatus(EventItemStatus.Pending(eventColors))
            }
        }
    }
}
