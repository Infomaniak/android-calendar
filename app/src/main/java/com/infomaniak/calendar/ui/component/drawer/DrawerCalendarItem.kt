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

import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.ui.component.drawer.model.CalendarUi
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.utils.toCalendarColorsUi
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarColors
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.CalendarId
import com.infomaniak.multiplatform_core.account.domain.model.AccountId

@Composable
fun DrawerCalendarItem(
    calendar: CalendarUi,
    onCalendarVisibilityChange: (CalendarId, Boolean) -> Unit,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
    onCalendarLongClick: (CalendarId) -> Unit = {},
) {
    SegmentedListItem(
        onClick = { onCalendarVisibilityChange(calendar.id, !calendar.isVisible) },
        onLongClick = { onCalendarLongClick(calendar.id) },
        shapes = shapes,
        modifier = modifier.semantics {
            role = Role.Checkbox
            toggleableState = ToggleableState(calendar.isVisible)
        },
        leadingContent = {
            Checkbox(
                checked = calendar.isVisible,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = calendar.colors.sourceColor,
                    checkmarkColor = calendar.colors.onSourceColor,
                    uncheckedColor = calendar.colors.sourceColor,
                ),
            )
        },
        verticalAlignment = Alignment.CenterVertically,
        colors = ListItemDefaults.drawerColors,
    ) {
        Text(calendar.displayName)
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

        DrawerCalendarItem(
            calendar = calendarPreview,
            onCalendarVisibilityChange = { _, _ -> },
            shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
        )
    }
}
