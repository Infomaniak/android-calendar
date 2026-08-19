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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.day.component.HourGrid
import com.infomaniak.calendar.components.day.component.HourLabelOverhang
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState

/** The hour grid of a single day, scrolling vertically over the whole 24 hours. */
@Composable
fun DayTimeline(state: DayTimelineState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(state.scrollState)
            .padding(top = HourLabelOverhang),
    ) {
        HourGrid(state = state, modifier = Modifier.fillMaxWidth())
    }
}

@Preview
@Composable
private fun DayTimelinePreview() {
    Surface {
        DayTimeline(state = rememberDayTimelineState(scrollState = rememberScrollState(initial = 430)))
    }
}
