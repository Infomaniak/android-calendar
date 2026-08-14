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
package com.infomaniak.calendar.ui.component.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.ui.component.drawer.model.CalendarUi
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.utils.toCalendarColorsUi
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarColors
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId

@Composable
fun DrawerCalendarItem(
    calendar: CalendarUi,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isChecked by remember(calendar.isVisible) { mutableStateOf(calendar.isVisible) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = isChecked,
                onValueChange = {
                    isChecked = it
                    onCalendarVisibilityChange(calendar.id, it)
                },
                role = Role.Checkbox,
            )
            .padding(all = Margin.Small),
        horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = calendar.colors.sourceColor,
                checkmarkColor = calendar.colors.onSourceColor,
                uncheckedColor = calendar.colors.sourceColor,
            ),
        )

        Text(text = calendar.displayName, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
private fun DrawerCalendarItemPreview() {
    CalendarThemeForPreview {
        val calendarPreview = CalendarUi(
            id = CalendarId("1"),
            accountId = AccountId(1),
            displayName = "Work",
            colors = CalendarColors.from(0xFF2196F3.toInt()).toCalendarColorsUi(),
            isVisible = true,
        )

        DrawerCalendarItem(calendar = calendarPreview, onCalendarVisibilityChange = { _, _ -> })
    }
}
