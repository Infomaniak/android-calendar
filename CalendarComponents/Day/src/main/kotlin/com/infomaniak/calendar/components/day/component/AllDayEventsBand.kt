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
package com.infomaniak.calendar.components.day.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.day.DayTimelineDefaults
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.event.component.cardStripes
import com.infomaniak.calendar.components.event.toEventItemStatus
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme

private val ChipHeight = 32.dp

/** A chip narrower than this has no room left to read a title in. */
private val MinChipWidth = 120.dp
private const val WHOLE_ROWS = 2
private val NextRowPeek = 12.dp

@Composable
internal fun AllDayEventsBand(
    events: List<EventUi.Normal>,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (events.isEmpty()) return

    val spacing = EsdsTheme.spacing.xs

    Row(modifier = modifier) {
        Text(
            text = stringResource(R.string.allDayLabel),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            // Held against the chips it names, at the gutter's end rather than at the screen's edge.
            textAlign = TextAlign.End,
            // The label stays inside the gutter so the chips still begin where the events do.
            modifier = Modifier
                .width(DayTimelineDefaults.HourGutterWidth)
                .padding(end = EsdsTheme.spacing.lg),
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(MinChipWidth),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier
                .weight(1f)
                .padding(end = DayTimelineDefaults.TimelineEndPadding)
                .heightIn(max = ChipHeight * WHOLE_ROWS + spacing * WHOLE_ROWS + NextRowPeek),
        ) {
            items(events, key = { it.id }) { event ->
                AllDayEventChip(event, onClick = { onEventClick(event) })
            }
        }
    }
}

@Composable
private fun AllDayEventChip(event: EventUi.Normal, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val status = event.toEventItemStatus()

    Card(
        onClick = onClick,
        colors = status.cardColors(),
        border = status.cardBorder(),
        shape = EsdsTheme.radius.sm,
        modifier = modifier.height(ChipHeight),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xs),
            modifier = Modifier
                .cardStripes(status)
                .fillMaxHeight(),
        ) {
            EventAccentBar(status.accentBarColor())

            Text(
                text = event.title,
                style = MaterialTheme.typography.bodySmallEmphasized,
                fontWeight = FontWeight.Medium,
                textDecoration = status.textDecoration,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = EsdsTheme.spacing.md),
            )
        }
    }
}

/** Four chips: enough to see them fill the rows two at a time on a phone's width. */
@Preview
@Composable
private fun AllDayEventsBandPreview() {
    Surface {
        AllDayEventsBand(events = previewDayEvents.allDay, onEventClick = {})
    }
}
