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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.preview.LocalEventColorsUiFactory
import com.infomaniak.calendar.components.foundation.utils.TimeFormatter.formatHours
import com.infomaniak.core.ui.compose.margin.Margin
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@Composable
fun EventItem(event: EventUi, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = event.colors.datavizContainerVariant,
            contentColor = event.colors.onDatavizContainerVariant,
        ),
        modifier = modifier,
    ) {
        Column(Modifier.padding(Margin.Mini), verticalArrangement = Arrangement.spacedBy(Margin.Micro)) {
            Text("${event.start.formatHours()} - ${event.end.formatHours()}")
            Text(event.title)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            EventItem(
                event = EventUi(
                    id = "1",
                    title = "Event title",
                    location = "Event location",
                    categories = "Event categories",
                    start = Clock.System.now(),
                    end = Clock.System.now().plus(3.hours),
                    colors = LocalEventColorsUiFactory.current.create(0xFF0098FF.toInt()),
                ),
            )
        }
    }
}
