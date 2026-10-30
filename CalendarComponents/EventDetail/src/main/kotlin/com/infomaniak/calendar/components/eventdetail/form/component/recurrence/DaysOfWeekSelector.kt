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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatShortDayName
import com.infomaniak.designsystem.core.theme.EsdsTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.toKotlinDayOfWeek
import java.time.temporal.WeekFields

@Composable
internal fun DaysOfWeekSelector(
    selectedDays: Set<DayOfWeek>,
    onSelectedDaysChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalLocale.current.platformLocale
    val daysOfWeek = remember(locale) {
        val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek.toKotlinDayOfWeek()
        DayOfWeek.entries.let { it.drop(firstDayOfWeek.ordinal) + it.take(firstDayOfWeek.ordinal) }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xs), modifier = modifier) {
        daysOfWeek.forEach { dayOfWeek ->
            ToggleButton(
                checked = dayOfWeek in selectedDays,
                onCheckedChange = { isChecked -> onSelectedDaysChange(if (isChecked) selectedDays + dayOfWeek else selectedDays - dayOfWeek) },
                contentPadding = PaddingValues(),
                modifier = Modifier.weight(1f),
            ) {
                Text(dayOfWeek.formatShortDayName(), maxLines = 1, overflow = TextOverflow.Clip)
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            DaysOfWeekSelector(selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), onSelectedDaysChange = {})
        }
    }
}
