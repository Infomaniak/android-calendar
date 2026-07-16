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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.component.DateState
import com.infomaniak.calendar.components.foundation.component.DayCircle
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Composable
internal fun Day(
    dateState: DateState,
    dateNumber: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(Margin.Micro),
        contentAlignment = Alignment.Center,
    ) {
        DayCircle(
            state = dateState,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable { onClick() },
        ) {
            Text(
                text = dateNumber,
                //TODO: Font Static/Body Large
            )
        }
    }
}

@Composable
@Preview
private fun DayPreview() {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val previewDaySize = 32.dp

    Surface {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(DateState.Selected.name, style = MaterialTheme.typography.labelSmall)
                Day(
                    dateState = DateState.Selected,
                    dateNumber = today.day.toString(),
                    onClick = {},
                    modifier = Modifier.size(previewDaySize),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(DateState.Today.name, style = MaterialTheme.typography.labelSmall)
                Day(
                    dateState = DateState.Today,
                    dateNumber = today.day.toString(),
                    onClick = {},
                    modifier = Modifier.size(previewDaySize),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(DateState.None.name, style = MaterialTheme.typography.labelSmall)
                Day(
                    dateState = DateState.None,
                    dateNumber = today.day.toString(),
                    onClick = {},
                    modifier = Modifier.size(previewDaySize),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(DateState.NotMonth.name, style = MaterialTheme.typography.labelSmall)
                Day(
                    dateState = DateState.NotMonth,
                    dateNumber = today.day.toString(),
                    onClick = {},
                    modifier = Modifier.size(previewDaySize),
                )
            }
        }
    }
}
