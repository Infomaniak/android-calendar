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
package com.infomaniak.calendar.components.foundation.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.component.DayCircle
import com.infomaniak.designsystem.core.theme.EsdsTheme

enum class DateState(
    val containerColor: @Composable () -> Color,
    val contentColor: @Composable () -> Color,
    val borderColor: @Composable () -> Color,
    val showDots: Boolean = true,
) {
    Today(
        containerColor = { Color.Transparent },
        contentColor = { MaterialTheme.colorScheme.primary },
        borderColor = { MaterialTheme.colorScheme.primary },
        showDots = false,
    ),
    Selected(
        containerColor = { MaterialTheme.colorScheme.primary },
        contentColor = { MaterialTheme.colorScheme.onPrimary },
        borderColor = { Color.Transparent },
        showDots = false,
    ),
    None(
        containerColor = { Color.Transparent },
        contentColor = { MaterialTheme.colorScheme.onSurface },
        borderColor = { Color.Transparent },
    ),
    NotMonth(
        containerColor = { Color.Transparent },
        contentColor = { MaterialTheme.colorScheme.onSurface.copy(EsdsTheme.opacity.medium) },
        borderColor = { Color.Transparent },
        showDots = false,
    ),
}

@Preview
@Composable
private fun Preview() {
    @Composable
    fun Item(state: DateState) {
        DayCircle(
            state = state,
            modifier = Modifier.size(40.dp),
        ) {
            Text(
                text = "1",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }

    MaterialTheme {
        Surface {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(DateState.None.name)
                    Item(DateState.None)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(DateState.Selected.name)
                    Item(DateState.Selected)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(DateState.Today.name)
                    Item(DateState.Today)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(DateState.NotMonth.name)
                    Item(DateState.NotMonth)
                }
            }
        }
    }
}
