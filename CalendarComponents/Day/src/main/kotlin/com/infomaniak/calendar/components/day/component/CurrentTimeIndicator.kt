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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.day.model.MINUTES_PER_HOUR
import com.infomaniak.calendar.components.day.state.TimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState
import com.infomaniak.calendar.components.foundation.component.CurrentTimeLine
import kotlinx.datetime.LocalDateTime

internal val LocalDateTime.minuteOfDay: Int get() = hour * MINUTES_PER_HOUR + minute

@Composable
internal fun CurrentTimeIndicator(minuteOfDay: Int, state: TimelineState, modifier: Modifier = Modifier) {
    CurrentTimeLine(modifier = modifier, lineY = { state.verticalOffsetOf(minuteOfDay).toPx() })
}

/** Room for the dot, which hangs off the start edge. */
private val PreviewStartPadding = 16.dp

@Preview(heightDp = 240)
@Composable
private fun CurrentTimeIndicatorPreview() {
    val state = rememberDayTimelineState({})
    CurrentTimeIndicator(
        minuteOfDay = 90,
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = PreviewStartPadding),
    )
}

@Preview(heightDp = 240)
@Composable
private fun CurrentTimeIndicatorWithBackgroundPreview() {
    Surface {
        val state = rememberDayTimelineState({})

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = PreviewStartPadding),
        ) {
            HourLines(state = state, modifier = Modifier.fillMaxWidth())
            CurrentTimeIndicator(minuteOfDay = 90, state = state, modifier = Modifier.matchParentSize())
        }
    }
}
