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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.component.DayCircle
import com.infomaniak.calendar.components.foundation.state.DateState
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatShortDayName
import com.infomaniak.core.common.utils.today
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

/** What sits at the top of a day's column in a multi-day timeline. Placeholder, design TBD. */
@Composable
internal fun DayColumnHeader(date: LocalDate, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xs),
        modifier = modifier.padding(vertical = EsdsTheme.spacing.sm),
    ) {
        Text(
            text = date.formatShortDayName(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )

        DayCircle(
            state = if (date == Clock.today()) DateState.Today else DateState.None,
            modifier = Modifier.size(32.dp),
        ) {
            Text(text = date.day.toString(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview
@Composable
private fun DayColumnHeaderPreview() {
    Surface {
        DayColumnHeader(date = Clock.today())
    }
}
