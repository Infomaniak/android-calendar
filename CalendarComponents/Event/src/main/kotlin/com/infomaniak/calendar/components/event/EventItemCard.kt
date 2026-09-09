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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.event.component.cardStripes
import com.infomaniak.calendar.components.foundation.preview.LocalEventColorsUiFactory
import com.infomaniak.core.ui.compose.margin.Margin

@Composable
fun EventItemCard(
    status: EventItemStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = EventItemCardDefaults.ContentPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardShape = MaterialTheme.shapes.small

    Box(modifier = modifier.height(IntrinsicSize.Min), propagateMinConstraints = true) {
        // Card with onClick enforces minimumInteractiveComponentSize() which makes small events too big compared to the design.
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Card(
                onClick = onClick,
                colors = status.cardColors(),
                border = status.cardBorder(),
                shape = cardShape,
            ) {
                Column(
                    modifier = Modifier
                        .cardStripes(status)
                        .padding(contentPadding),
                    content = content,
                )
            }
        }

        AccentBar(
            color = status.eventColors.calendarSourceColor,
            shape = cardShape,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

object EventItemCardDefaults {
    val HorizontalPaddingValue = Margin.Small
    val VerticalPaddingValue = Margin.Mini

    val ContentPadding = PaddingValues(horizontal = HorizontalPaddingValue, vertical = VerticalPaddingValue)
}


@Preview
@Composable
private fun EventItemCardPreview() {
    val status = EventItemStatus.Default(LocalEventColorsUiFactory.current.create(0x0))

    MaterialTheme {
        Surface {
            EventItemCard(
                status = status,
                onClick = {},
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.padding(8.dp),
            ) {
                // Sample content for the preview
                Column {
                    androidx.compose.material3.Text(text = "Event Title", style = MaterialTheme.typography.titleMedium)
                    androidx.compose.material3.Text(text = "Event Details", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
