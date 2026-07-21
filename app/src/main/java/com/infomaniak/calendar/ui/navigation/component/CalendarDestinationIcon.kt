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

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.navigation.NavDestination

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
        Icon(painter = painterResource(selectedIcon.icon), contentDescription = contentDescription)
    }
}

private data class SelectedIconData(
    @DrawableRes val icon: Int,
    val contentDescription: Int,
)

private fun getSelectedIcon(lastMainNavigationSelected: NavDestination.CalendarView): SelectedIconData {
    return when (lastMainNavigationSelected) {
        is NavDestination.CalendarView.Planning -> SelectedIconData(R.drawable.ic_rows_two, R.string.planningTitle)
        is NavDestination.CalendarView.Day -> SelectedIconData(R.drawable.ic_overline_rectangle_underline, R.string.dayTitle)
        is NavDestination.CalendarView.ThreeDays -> SelectedIconData(R.drawable.ic_columns_three, R.string.threeDaysTitle)
        is NavDestination.CalendarView.Week -> SelectedIconData(R.drawable.ic_columns_four, R.string.weekTitle)
        is NavDestination.CalendarView.Month -> SelectedIconData(R.drawable.ic_grid_three_two, R.string.monthTitle)
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
