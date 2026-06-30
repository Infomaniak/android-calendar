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
import androidx.annotation.FloatRange
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.event.component.cardStripes
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.preview.LocalEventColorsUiFactory
import com.infomaniak.calendar.components.foundation.utils.TimeFormatter.formatHours
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.ui.compose.margin.Margin
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Composable
fun EventItem(event: EventUi, modifier: Modifier = Modifier) {
    EventItem(event.start, event.end, event.title, event.toEventItemStatus(), event.toEventIcons(), modifier)
}

@Composable
internal fun EventItem(
    start: Instant,
    end: Instant,
    title: String,
    status: EventItemStatus,
    trailingIcons: Set<EventIcons>,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = status.cardColors(),
        border = status.cardBorder(),
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Row(
            Modifier
                .cardStripes(status)
                .padding(Margin.Mini),
            horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Margin.Micro),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    "${start.formatHours()} - ${end.formatHours()}",
                    style = MaterialTheme.typography.labelSmall,
                    textDecoration = status.textDecoration,
                )
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmallEmphasized,
                    fontWeight = FontWeight.Medium,
                    textDecoration = status.textDecoration,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (trailingIcons.isNotEmpty()) TrailingIcons(trailingIcons)
        }

        EventSizeSpacer(end - start)
    }
}

@Composable
private fun TrailingIcons(trailingIcons: Set<EventIcons>) {
    Row(horizontalArrangement = Arrangement.spacedBy(Margin.Micro)) {
        EventIcons.entries.forEach {
            if (it in trailingIcons) it.TrailingIcon()
        }
    }
}

private fun EventUi.toEventItemStatus(): EventItemStatus {
    if (status == EventStatus.Cancelled) return EventItemStatus.Declined(colors)
    val me = attendees.me ?: return EventItemStatus.Default(colors)

    return when (me.status) {
        ParticipationStatus.Accepted -> EventItemStatus.Default(colors)
        ParticipationStatus.Declined -> EventItemStatus.Declined(colors)
        ParticipationStatus.Tentative -> EventItemStatus.Maybe(colors)
        ParticipationStatus.NeedsAction -> EventItemStatus.Pending(colors)
    }
}

private fun EventUi.toEventIcons(): Set<EventIcons> = buildSet {
    if (location != null) add(EventIcons.Location)
    if (attendees.all.isNotEmpty()) add(EventIcons.Attendees)
    // TODO: Detect kMeet links
}

@Immutable
sealed class EventItemStatus(
    val cardColors: @Composable () -> CardColors,
    val cardBorder: @Composable () -> BorderStroke? = { null },
    val stripesColor: @Composable () -> Color? = { null },
    val textDecoration: TextDecoration? = null,
) {
    abstract val eventColors: EventColorsUi

    data class Default(override val eventColors: EventColorsUi) : EventItemStatus(
        cardColors = { containerColors(eventColors) },
    )

    data class Maybe(override val eventColors: EventColorsUi) : EventItemStatus(
        cardColors = { containerColors(eventColors) },
        stripesColor = { eventColors.onDatavizContainerVariant.copy(alpha = 0.1f) },
    )

    data class Declined(override val eventColors: EventColorsUi) : EventItemStatus(
        cardColors = { containerColors(eventColors, contentAlpha = 0.5f) },
        textDecoration = TextDecoration.LineThrough,
    )

    data class Pending(override val eventColors: EventColorsUi) : EventItemStatus(
        cardColors = { containerVariantColors(eventColors) },
        cardBorder = { BorderStroke(2.dp, eventColors.onDatavizContainer) },
    )

    companion object {
        @Composable
        private fun containerVariantColors(eventColors: EventColorsUi): CardColors = CardDefaults.cardColors(
            containerColor = eventColors.datavizContainer,
            contentColor = eventColors.onDatavizContainer,
        )

        @Composable
        private fun containerColors(
            eventColors: EventColorsUi,
            @FloatRange(0.0, 1.0) contentAlpha: Float = 1f,
        ): CardColors = CardDefaults.cardColors(
            containerColor = eventColors.datavizContainerVariant,
            contentColor = eventColors.onDatavizContainerVariant.copyIfNeeded(contentAlpha),
        )

        /**
         * Skips instantiations cause by copy if we're modifying alpha to be the same value as it already was. Useful for all
         * common cases where we set alpha to 1.
         */
        private fun Color.copyIfNeeded(@FloatRange(0.0, 1.0) alpha: Float): Color {
            return if (alpha == this.alpha) this else copy(alpha)
        }
    }
}

enum class EventIcons(
    @DrawableRes private val icon: Int,
    @StringRes private val contentDescription: Int,
) {
    Location(R.drawable.ic_map_pin, R.string.contentDescriptionHasLocation),
    Kmeet(R.drawable.ic_product_kmeet, R.string.contentDescriptionHasKMeetLink),
    Attendees(R.drawable.ic_users_stacked, R.string.contentDescriptionHasAttendees);

    @Composable
    fun TrailingIcon() {
        Icon(painterResource(icon), stringResource(contentDescription), modifier = Modifier.size(16.dp))
    }
}

@Preview
@Composable
private fun Preview() {
    @Composable
    fun EventItemForStatus(status: EventItemStatus, title: String = "Event title") {
        EventItem(
            start = Clock.System.now(),
            end = Clock.System.now() + 1.hours,
            title = title,
            status = status,
            trailingIcons = EventIcons.entries.toSet(),
        )
    }

    MaterialTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val eventColors = LocalEventColorsUiFactory.current.create(0x0)

                EventItemForStatus(
                    status = EventItemStatus.Default(eventColors),
                    title = "How to not get fired. An important guide on how to navigate the workspace",
                )
                EventItemForStatus(EventItemStatus.Declined(eventColors))
                EventItemForStatus(EventItemStatus.Maybe(eventColors))
                EventItemForStatus(EventItemStatus.Pending(eventColors))
            }
        }
    }
}
