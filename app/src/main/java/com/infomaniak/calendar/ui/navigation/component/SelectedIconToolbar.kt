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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.navigation.NavDestination

@Composable
fun CurrentDestinationIcon(
    currentDestination: NavDestination.PlageDateDestination,
    onMenuExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIcon = getSelectedIcon(lastMainNavigationSelected = currentDestination)
    val labelText = stringResource(selectedIcon.contentDescription)
    val contentDescription = stringResource(R.string.toolbarCurrentViewButtonContentDescription, labelText)

    IconButton(onClick = onMenuExpanded, modifier = modifier) {
        Icon(imageVector = selectedIcon.icon, contentDescription = contentDescription)
    }
}

private data class SelectedIconData(
    val icon: ImageVector,
    val contentDescription: Int,
)

@Composable
private fun getSelectedIcon(lastMainNavigationSelected: NavDestination.PlageDateDestination): SelectedIconData {
    return when (lastMainNavigationSelected) {
        is NavDestination.PlageDateDestination.Day -> SelectedIconData(Icons.Outlined.ViewDay, R.string.dayTitles)
        is NavDestination.PlageDateDestination.ThreeDays -> SelectedIconData(Icons.Outlined.ViewDay, R.string.threeDaysTitle)
        is NavDestination.PlageDateDestination.Week -> SelectedIconData(Icons.Outlined.ViewWeek, R.string.weekTitle)
        is NavDestination.PlageDateDestination.Month -> SelectedIconData(Icons.Outlined.CalendarMonth, R.string.monthTitle)
        is NavDestination.PlageDateDestination.Planning -> SelectedIconData(Icons.Outlined.ViewAgenda, R.string.planningTitle)
    }
}
