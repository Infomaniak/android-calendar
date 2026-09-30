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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.day.component.HourGrid
import com.infomaniak.calendar.components.day.component.HourLabelOverhang
import com.infomaniak.calendar.components.day.state.DayTimelineState
import com.infomaniak.calendar.components.day.state.rememberDayTimelineState

@Composable
fun WeekPager(
    modifier: Modifier = Modifier,
    state: DayTimelineState = rememberDayTimelineState(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    BoxWithConstraints(
        modifier = modifier
            .verticalScroll(state.scrollState, enabled = !state.isPinching)
            .padding(
                top = HourLabelOverhang + contentPadding.calculateTopPadding(),
                bottom = DayTimelineDefaults.BottomPadding + contentPadding.calculateBottomPadding(),
            )
            // Inside the padding: a pinch reads its own y as an hour, so it has to start
            // counting where the first hour line is drawn, not where the padding begins.
            .pinchToZoom(state),
    ) {
        val columnCount = 3
        val columnWidth = (this@BoxWithConstraints.maxWidth - DayTimelineDefaults.HourGutterWidth) / columnCount

        HourGrid(
            state = state,
            columnWidth = columnWidth,
            columnCount = columnCount,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            WeekPager()
        }
    }
}
