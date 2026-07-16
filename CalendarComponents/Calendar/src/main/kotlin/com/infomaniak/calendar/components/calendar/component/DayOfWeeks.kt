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
package com.infomaniak.calendar.components.calendar.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.DayFormatter.toSimpleDayName
import com.infomaniak.core.ui.compose.margin.Margin
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Composable
internal fun DaysOfWeekTitle(firstDayOfWeek: DayOfWeek, modifier: Modifier = Modifier) {
    val locale = LocalLocale.current.platformLocale

    Row(modifier = modifier.fillMaxWidth()) {
        daysOfWeek(firstDayOfWeek).forEach { dayOfWeek ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(Margin.Micro),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = dayOfWeek.toSimpleDayName(locale))
            }
        }
    }
}

@Composable
@Preview
private fun DaysOfWeekTitlePreview() {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Surface {
        DaysOfWeekTitle(firstDayOfWeek = today.dayOfWeek)
    }
}
