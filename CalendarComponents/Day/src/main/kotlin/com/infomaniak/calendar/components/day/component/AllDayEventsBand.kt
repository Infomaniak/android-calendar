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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.day.TimelineDefaults
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.event.highlight.EventHighlightState
import com.infomaniak.calendar.components.event.highlight.eventDropTarget
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.common.utils.today
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlin.time.Clock
import kotlinx.datetime.LocalDate

private val ChipHeight = 32.dp

/** A chip narrower than this has no room left to read a title in. */
private val MinChipWidth = 120.dp
private const val WHOLE_ROWS = 2
private val NextRowPeek = 12.dp

/** Room for two rows of chips, the top of a third one peeking below to tell more are scrolled to. */
internal val AllDayBandMaxHeight: Dp
    @Composable get() = ChipHeight * WHOLE_ROWS + AllDayEventSpacing * WHOLE_ROWS + NextRowPeek

internal val AllDayEventSpacing: Dp
    @Composable get() = EsdsTheme.spacing.xs

/**
 * The all-day events of [date], named from the hour gutter. With a [highlightState], a long press holds
 * a chip up to drag it, and the band is where an event can be dropped on [date], all day long.
 */
@Composable
internal fun AllDayEventsBand(
    date: LocalDate,
    events: List<EventSummaryUi.Normal>,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    highlightState: EventHighlightState? = null,
) {
    if (events.isEmpty()) return

    Row(modifier = modifier.then(highlightState?.let { Modifier.eventDropTarget(it, date) } ?: Modifier)) {
        AllDayLabel()

        LazyVerticalGrid(
            columns = GridCells.Adaptive(MinChipWidth),
            userScrollEnabled = highlightState?.isHeld != true,
            verticalArrangement = Arrangement.spacedBy(AllDayEventSpacing),
            horizontalArrangement = Arrangement.spacedBy(AllDayEventSpacing),
            modifier = Modifier
                .weight(1f)
                .padding(end = TimelineDefaults.TimelineEndPadding)
                .heightIn(max = AllDayBandMaxHeight),
        ) {
            itemsIndexed(
                items = events,
                key = { _, event -> event.id },
                // Only the last chip can end up alone on its row, and it takes the whole of it
                // rather than leaving the half it does not use empty.
                span = { index, _ -> GridItemSpan(if (index == events.lastIndex) maxCurrentLineSpan else 1) },
            ) { _, event ->
                AllDayEvent(event = event, onClick = { onEventClick(event) }, highlightState = highlightState)
            }
        }
    }
}

/** Names the all-day band from the hour gutter, so the chips still begin where the timed events do. */
@Composable
internal fun AllDayLabel(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.allDayLabel),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        // Held against the chips it names, at the gutter's end rather than at the screen's edge.
        textAlign = TextAlign.End,
        modifier = modifier
            .width(TimelineDefaults.HourGutterWidth)
            .padding(end = EsdsTheme.spacing.lg),
    )
}

/** Four chips: enough to see them fill the rows two at a time on a phone's width. */
@Preview
@Composable
private fun AllDayEventsBandPreview() {
    Surface {
        AllDayEventsBand(date = Clock.today(), events = previewDayEvents.allDay, onEventClick = {})
    }
}
