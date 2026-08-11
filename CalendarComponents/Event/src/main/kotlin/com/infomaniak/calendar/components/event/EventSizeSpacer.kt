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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.infomaniak.calendar.components.foundation.preview.LocalEventColorsUiFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private val MaxHeight = 50.dp
private val MinHeight = 0.dp
private val MinDuration = 15.minutes
private val MaxDuration = 2.hours

@Composable
internal fun EventSizeSpacer(duration: Duration, modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier
            .height(height = deriveHeightFrom(duration))
            .fillMaxWidth(),
    )
}

private fun deriveHeightFrom(duration: Duration): Dp = when {
    duration < MinDuration -> MinHeight
    duration > MaxDuration -> MaxHeight
    else -> {
        val fraction = (duration - MinDuration).inWholeSeconds.toFloat() / (MaxDuration - MinDuration).inWholeSeconds
        lerp(MinHeight, MaxHeight, fraction)
    }
}

private val previewCases = listOf(
    MinDuration to "min",
    45.minutes to "45 min",
    1.hours to "1 h",
    MaxDuration to "max",
)

@Preview(showBackground = true)
@Composable
private fun EventSizeSpacerPreview() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(16.dp),
        ) {
            previewCases.forEach { (duration, label) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(60.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                    EventSizeSpacer(
                        duration = duration,
                        modifier = Modifier.background(Color(0xFF6650A4)),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun EventSizeSpacerStackedPreview() {
    val now = Instant.parse("2025-01-01T09:00:00Z")
    val eventColors = LocalEventColorsUiFactory.current.create(0x0)

    MaterialTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                previewCases.forEach { (duration, label) ->
                    EventItem(
                        title = "Event ($label gap below)",
                        location = null,
                        status = EventItemStatus.Default(eventColors),
                        start = now,
                        end = now + duration,
                        isAllDay = false,
                        trailingIcons = emptySet(),
                    )
                }
            }
        }
    }
}
