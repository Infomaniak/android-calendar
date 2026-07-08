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
package com.infomaniak.calendar.ui.component.topAppBar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.state.LocalCurrentDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.LocalDate

@Composable
fun CurrentMonthTitle(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val visibleDate = LocalCurrentDayState.current?.visibleDate ?: return
    MonthYearTitle(date = visibleDate, onClick = onClick, modifier = modifier)
}

@Composable
private fun MonthYearTitle(date: LocalDate, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        onClick = onClick,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Margin.Mini)) {
            ProvideTextStyle(MaterialTheme.typography.titleLarge) { AnimatedMonthYearText(date = date) }
            Icon(painter = painterResource(R.drawable.ic_chevron), contentDescription = null)
        }
    }
}

@Preview
@Composable
private fun MonthYearTitlePreview() {
    CalendarThemeForPreview {
        MonthYearTitle(date = LocalDate(2026, 7, 8), onClick = { })
    }
}

@Preview(locale = "fr")
@Composable
private fun MonthYearTitleFrenchPreview() {
    CalendarThemeForPreview {
        MonthYearTitle(date = LocalDate(2025, 12, 25), onClick = { })
    }
}
