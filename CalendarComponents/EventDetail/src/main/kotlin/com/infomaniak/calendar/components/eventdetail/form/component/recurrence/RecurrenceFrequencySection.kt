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
package com.infomaniak.calendar.components.eventdetail.form.component.recurrence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.form.component.AmountTextField
import com.infomaniak.calendar.components.eventdetail.form.component.OptionsDropdown
import com.infomaniak.calendar.components.eventdetail.form.component.SectionTitle
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi.Rule.Frequency
import com.infomaniak.calendar.components.eventdetail.models.RecurrenceUi.Rule.MonthDay
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@Composable
internal fun RecurrenceFrequencySection(
    start: LocalDate,
    intervalTextFieldState: TextFieldState,
    interval: Int,
    selectedFrequency: Frequency,
    onFrequencySelect: (Frequency) -> Unit,
    selectedDaysOfWeek: Set<DayOfWeek>,
    onSelectedDaysOfWeekChange: (Set<DayOfWeek>) -> Unit,
    selectedMonthDay: MonthDay,
    onMonthDaySelect: (MonthDay) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SectionTitle(stringResource(R.string.recurrenceFrequencyTitle))

        Column(verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md)) {
            Row(horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md)) {
                AmountTextField(intervalTextFieldState)

                OptionsDropdown(
                    options = Frequency.entries,
                    selectedOption = selectedFrequency,
                    onOptionSelect = onFrequencySelect,
                    optionText = { frequency -> frequencyLabel(frequency, interval) },
                    modifier = Modifier.weight(1f),
                )
            }

            when (selectedFrequency) {
                Frequency.Daily -> Unit
                Frequency.Weekly -> DaysOfWeekSelector(selectedDaysOfWeek, onSelectedDaysOfWeekChange)
                Frequency.Monthly, Frequency.Yearly -> MonthDayDropdown(
                    start = start,
                    isYearly = selectedFrequency == Frequency.Yearly,
                    selectedMonthDay = selectedMonthDay,
                    onMonthDaySelect = onMonthDaySelect,
                )
            }
        }
    }
}

/** `Week`, agreeing with [interval]. */
@Composable
private fun frequencyLabel(frequency: Frequency, interval: Int): String {
    val locale = LocalLocale.current.platformLocale
    return pluralStringResource(frequency.unitRes, interval).replaceFirstChar { it.titlecase(locale) }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            RecurrenceFrequencySection(
                start = LocalDate(2026, 12, 21),
                intervalTextFieldState = rememberTextFieldState("1"),
                interval = 1,
                selectedFrequency = Frequency.Weekly,
                onFrequencySelect = {},
                selectedDaysOfWeek = setOf(DayOfWeek.MONDAY),
                onSelectedDaysOfWeekChange = {},
                selectedMonthDay = MonthDay.StartDay,
                onMonthDaySelect = {},
            )
        }
    }
}
