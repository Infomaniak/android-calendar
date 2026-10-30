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
package com.infomaniak.calendar.components.eventdetail.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.form.component.recurrence.RecurrenceEndSection
import com.infomaniak.calendar.components.eventdetail.form.component.recurrence.RecurrenceFrequencySection
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.datetime.LocalDate

@Composable
fun CustomRecurrence(state: CustomRecurrenceState, modifier: Modifier = Modifier, contentPadding: PaddingValues = PaddingValues()) {
    Column(
        verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.lg),
        modifier = modifier.padding(contentPadding),
    ) {
        RecurrenceFrequencySection(
            start = state.start,
            intervalTextFieldState = state.intervalTextFieldState,
            interval = state.interval ?: 1,
            selectedFrequency = state.frequency.value,
            onFrequencySelect = { frequency -> state.frequency.value = frequency },
            selectedDaysOfWeek = state.daysOfWeek.value,
            onSelectedDaysOfWeekChange = { daysOfWeek -> state.daysOfWeek.value = daysOfWeek },
            selectedMonthDay = state.monthDay.value,
            onMonthDaySelect = { monthDay -> state.monthDay.value = monthDay },
        )

        HorizontalDivider()

        RecurrenceEndSection(
            selectedEndType = state.endType.value,
            onEndTypeSelect = { endType -> state.endType.value = endType },
            countTextFieldState = state.countTextFieldState,
            untilDate = state.untilDate.value,
            onUntilDateSelect = { date -> state.untilDate.value = date },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            CustomRecurrence(
                state = rememberCustomRecurrenceState(start = LocalDate(2026, 12, 21), initialRecurrence = RecurrenceUi.None),
                contentPadding = PaddingValues(16.dp),
            )
        }
    }
}
