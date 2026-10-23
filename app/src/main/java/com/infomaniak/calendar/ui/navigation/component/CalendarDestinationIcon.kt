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
package com.infomaniak.calendar.ui.navigation.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.ColumnsFour
import com.infomaniak.designsystem.icons.symbols.ColumnsThree
import com.infomaniak.designsystem.icons.symbols.GridThreeTwo
import com.infomaniak.designsystem.icons.symbols.RectangleOverlineUnderline
import com.infomaniak.designsystem.icons.symbols.RowsTwo
import com.infomaniak.designsystem.icons.symbols.Symbol

@Composable
fun CurrentDestinationIcon(
    currentDestination: NavDestination.CalendarView,
    onMenuExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIcon = getSelectedIcon(lastMainNavigationSelected = currentDestination)
    val labelText = stringResource(selectedIcon.contentDescription)
    val contentDescription = stringResource(R.string.contentDescriptionToolbarCurrentViewButton, labelText)

    IconButton(onClick = onMenuExpanded, modifier = modifier) {
        Icon(imageVector = selectedIcon.icon.Medium, contentDescription = contentDescription)
    }
}

private data class SelectedIconData(
    val icon: Symbol,
    val contentDescription: Int,
)

private fun getSelectedIcon(lastMainNavigationSelected: NavDestination.CalendarView): SelectedIconData {
    return when (lastMainNavigationSelected) {
        is NavDestination.CalendarView.Planning -> SelectedIconData(EsdsIcons.RowsTwo, R.string.planningTitle)
        is NavDestination.CalendarView.Day -> SelectedIconData(EsdsIcons.RectangleOverlineUnderline, R.string.dayTitle)
        is NavDestination.CalendarView.ThreeDays -> SelectedIconData(EsdsIcons.ColumnsThree, R.string.threeDaysTitle)
        is NavDestination.CalendarView.Week -> SelectedIconData(EsdsIcons.ColumnsFour, R.string.weekTitle)
        is NavDestination.CalendarView.Month -> SelectedIconData(EsdsIcons.GridThreeTwo, R.string.monthTitle)
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            CurrentDestinationIcon(
                currentDestination = NavDestination.CalendarView.Day,
                onMenuExpanded = {},
            )
        }
    }
}
