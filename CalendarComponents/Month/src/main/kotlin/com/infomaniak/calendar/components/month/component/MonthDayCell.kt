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
package com.infomaniak.calendar.components.month.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.foundation.component.DayCircle
import com.infomaniak.calendar.components.foundation.state.DateState
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatFullDateWithYear
import com.infomaniak.calendar.components.foundation.utils.verticalEndDivider
import com.infomaniak.calendar.components.month.layout.MonthDefaults
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.datetime.LocalDate

/**
 * A day of the month grid: its number at the top, the today tint over the whole cell, and its end separator. The whole
 * cell opens the day, and is announced with its [eventCount], hidden events included.
 */
@Composable
internal fun MonthDayCell(
    date: LocalDate,
    isInMonth: Boolean,
    isToday: Boolean,
    eventCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateState = when {
        !isInMonth -> DateState.NotMonth
        isToday -> DateState.Today
        else -> DateState.None
    }
    val description = dayDescription(date, eventCount)
    val todayDescription = if (isToday) stringResource(R.string.contentDescriptionToday) else null

    Box(
        modifier = modifier
            .then(if (isToday) Modifier.background(todayTint()) else Modifier)
            .verticalEndDivider()
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                todayDescription?.let { stateDescription = it }
            }
            .padding(MonthDefaults.DayNumberPadding),
        contentAlignment = Alignment.TopCenter,
    ) {
        DayCircle(state = dateState, modifier = Modifier.size(MonthDefaults.DayNumberSize)) {
            Text(text = date.day.toString(), style = MonthDefaults.DayNumberStyle)
        }
    }
}

/** Tints the whole cell, the only today cue left on a day outside the month, whose number is dimmed instead. */
@Composable
private fun todayTint() = MaterialTheme.colorScheme.primary.copy(alpha = EsdsTheme.opacity.subtle)

@Composable
private fun dayDescription(date: LocalDate, eventCount: Int): String {
    val fullDate = date.formatFullDateWithYear()
    val events = if (eventCount == 0) null else pluralStringResource(R.plurals.contentDescriptionEventCount, eventCount, eventCount)
    return remember(fullDate, events) { listOfNotNull(fullDate, events).joinToString() }
}

@Preview
@Composable
private fun Preview() {
    val date = LocalDate(2026, 5, 22)

    MaterialTheme {
        Surface {
            Row {
                val cellModifier = Modifier.size(width = 56.dp, height = 80.dp)
                MonthDayCell(date, isInMonth = true, isToday = false, eventCount = 0, onClick = {}, modifier = cellModifier)
                MonthDayCell(date, isInMonth = true, isToday = true, eventCount = 2, onClick = {}, modifier = cellModifier)
                MonthDayCell(date, isInMonth = false, isToday = false, eventCount = 0, onClick = {}, modifier = cellModifier)
                MonthDayCell(date, isInMonth = false, isToday = true, eventCount = 0, onClick = {}, modifier = cellModifier)
            }
        }
    }
}
