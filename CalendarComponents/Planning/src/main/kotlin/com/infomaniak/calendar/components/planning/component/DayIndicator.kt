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
package com.infomaniak.calendar.components.planning.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.component.DateState
import com.infomaniak.calendar.components.foundation.component.DayCircle
import com.infomaniak.core.ui.compose.margin.Margin

@Composable
internal fun DayIndicator(dayName: String, dayNumber: Int, state: DateState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(dayName, style = MaterialTheme.typography.bodyMedium)

        DayCircle(
            state = state,
            modifier = Modifier
                .size(48.dp)
                .padding(Margin.Micro),
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("None", style = MaterialTheme.typography.labelSmall)
                    DayIndicator(
                        dayName = "Mon",
                        dayNumber = 1,
                        state = DateState.None,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Selected", style = MaterialTheme.typography.labelSmall)
                    DayIndicator(
                        dayName = "Mon",
                        dayNumber = 1,
                        state = DateState.Selected,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Today", style = MaterialTheme.typography.labelSmall)
                    DayIndicator(
                        dayName = "Mon",
                        dayNumber = 1,
                        state = DateState.Today,
                    )
                }
            }
        }
    }
}
