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
package com.infomaniak.calendar.components.days.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.common.utils.today
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** One equally wide column per date. */
@Composable
internal fun DaysRow(
    dates: List<LocalDate>,
    modifier: Modifier = Modifier,
    content: @Composable (LocalDate) -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        dates.forEach { date ->
            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.weight(1f)) {
                content(date)
            }
        }
    }
}

@Preview
@Composable
private fun ThreeDaysPreview() {
    DaysRowSample(dayCount = 3)
}

@Preview
@Composable
private fun WeekPreview() {
    DaysRowSample(dayCount = 7)
}

@Composable
private fun DaysRowSample(dayCount: Int) {
    val today = Clock.today()
    Surface {
        DaysRow(dates = List(dayCount) { today.plus(it, DateTimeUnit.DAY) }) { date ->
            DayColumnHeader(date = date)
        }
    }
}
