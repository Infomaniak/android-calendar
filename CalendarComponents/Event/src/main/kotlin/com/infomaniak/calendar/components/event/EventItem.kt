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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.preview.LocalEventColorsUiFactory
import com.infomaniak.calendar.components.foundation.utils.TimeFormatter.formatHours
import com.infomaniak.core.ui.compose.margin.Margin
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Composable
fun EventItem(event: EventUi, modifier: Modifier = Modifier) {
    EventItem(event.start, event.end, event.title, event.toEventItemStatus(), modifier)
}

@Composable
private fun EventItem(
    start: Instant,
    end: Instant,
    title: String,
    status: EventItemStatus,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = status.cardColors(),
        border = status.cardBorder(),
        modifier = modifier,
    ) {
        Column(Modifier.padding(Margin.Mini), verticalArrangement = Arrangement.spacedBy(Margin.Micro)) {
            Text("${start.formatHours()} - ${end.formatHours()}")
            Text(title)
        }
    }
}

// TODO
private fun EventUi.toEventItemStatus(): EventItemStatus {
    return when (status) {
        EventStatus.Confirmed -> EventItemStatus.Default(colors)
        EventStatus.Tentative -> EventItemStatus.Default(colors)
        EventStatus.Cancelled -> EventItemStatus.Declined(colors)
    }
}

@Composable
private fun containerColors(eventColors: EventColorsUi): CardColors = CardDefaults.cardColors(
    containerColor = eventColors.datavizContainerVariant,
    contentColor = eventColors.onDatavizContainerVariant,
)

@Composable
private fun containerVariantColors(eventColors: EventColorsUi): CardColors = CardDefaults.cardColors(
    containerColor = eventColors.datavizContainer,
    contentColor = eventColors.onDatavizContainer,
)

@Immutable
sealed class EventItemStatus(
    val cardColors: @Composable () -> CardColors,
    val cardBorder: @Composable () -> BorderStroke? = { null },
    val stripesColor: @Composable () -> Unit = { Color.Transparent },
) {
    abstract val eventColors: EventColorsUi

    data class Default(override val eventColors: EventColorsUi) : EventItemStatus(
        cardColors = { containerColors(eventColors) },
    )

    data class Maybe(override val eventColors: EventColorsUi) : EventItemStatus(
        cardColors = { containerColors(eventColors) },
        stripesColor = { eventColors.onDatavizContainerVariant.copy(alpha = 0.9f) },
    )

    data class Declined(override val eventColors: EventColorsUi) : EventItemStatus(
        cardColors = { containerColors(eventColors) },
    )

    data class Pending(override val eventColors: EventColorsUi) : EventItemStatus(
        cardColors = { containerVariantColors(eventColors) },
        cardBorder = { BorderStroke(1.dp, eventColors.onDatavizContainer) },
    )
}

@Preview
@Composable
private fun Preview() {
    @Composable
    fun EventItemForStatus(status: EventItemStatus) {
        EventItem(
            start = Clock.System.now(),
            end = Clock.System.now() + 1.hours,
            title = "Event title",
            status = status,
        )
    }

    MaterialTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val eventColors = LocalEventColorsUiFactory.current.create(0x0)

                EventItemForStatus(EventItemStatus.Default(eventColors))
                EventItemForStatus(EventItemStatus.Declined(eventColors))
                EventItemForStatus(EventItemStatus.Maybe(eventColors))
                EventItemForStatus(EventItemStatus.Pending(eventColors))
            }
        }
    }
}
