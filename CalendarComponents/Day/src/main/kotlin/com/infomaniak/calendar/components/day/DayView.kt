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
package com.infomaniak.calendar.components.day

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.day.component.AllDayEventsBand
import com.infomaniak.calendar.components.day.component.DayHeader
import com.infomaniak.calendar.components.day.layout.EventLayoutDefaults.DividerHeight
import com.infomaniak.calendar.components.day.model.DayEvents
import com.infomaniak.calendar.components.day.preview.previewDayEvents
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.core.common.utils.today
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

/**
 * A single day: the scrollable hour grid carrying the timed events, with its header and the all-day
 * events pinned over the top of it.
 *
 * The header floats rather than sitting above the grid, so the hours pass under it, and under
 * whatever the caller floats over the day: [topContentPadding] is the room that caller needs, and
 * [headerModifier] is where it hands in the background the header is read against. [timelineModifier]
 * reaches the grid alone, for the callers that blur what scrolls under them.
 */
@Composable
fun DayView(
    date: LocalDate,
    events: DayEvents,
    state: DayTimelineState,
    weekNumbering: WeekNumbering,
    onEventClick: (EventUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    headerModifier: Modifier = Modifier,
    timelineModifier: Modifier = Modifier,
    topContentPadding: Dp = 0.dp,
    headerTrailingContent: @Composable () -> Unit = {},
) {
    val density = LocalDensity.current
    // The header is measured rather than given a height: the all-day band grows with the events it
    // holds, and the hours have to start under whatever it ends up being.
    var headerHeight by remember { mutableStateOf(0.dp) }

    Box(modifier = modifier) {
        DayTimeline(
            date = date,
            events = events,
            state = state,
            onEventClick = onEventClick,
            topContentPadding = headerHeight + EsdsTheme.spacing.md,
            modifier = timelineModifier
                .fillMaxSize()
                .padding(horizontal = EsdsTheme.spacing.md),
        )

        Column {
            Column(
                verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md),
                modifier = headerModifier
                    .fillMaxWidth()
                    .onSizeChanged { headerHeight = with(density) { it.height.toDp() } }
                    .padding(top = topContentPadding),
            ) {
                DayHeader(
                    date = date,
                    weekNumbering = weekNumbering,
                    trailing = headerTrailingContent,
                    modifier = Modifier.padding(horizontal = EsdsTheme.spacing.md),
                )

                AllDayEventsBand(
                    events = events.allDay,
                    onEventClick = onEventClick,
                    modifier = Modifier.padding(horizontal = EsdsTheme.spacing.md),
                )

            }

            Spacer(
                modifier = Modifier
                    .height(DividerHeight)
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.outlineVariant),
            )
        }
    }
}

@Preview
@Composable
private fun DayViewPreview() {
    Surface {
        DayView(
            date = Clock.today(),
            events = previewDayEvents,
            state = rememberDayTimelineState(scrollState = rememberScrollState(initial = 430)),
            weekNumbering = WeekNumbering.ISO_8601,
            onEventClick = {},
        )
    }
}
