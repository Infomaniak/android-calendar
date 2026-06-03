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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.infomaniak.calendar.ui.component.CalendarFab
import com.infomaniak.calendar.ui.modifier.sharedElement
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.navigation.state.LocalToolbarScrollableState
import com.infomaniak.core.ui.compose.margin.Margin

private const val FLOATING_TOOLBARBAR_KEY = "FloatingToolbar"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalendarHorizontalFloatingToolbar(
    onNavigationButtonClicked: (NavDestination.PlageDateDestination) -> Unit,
    onCurrentDayClicked: () -> Unit,
    currentDestination: () -> NavDestination.PlageDateDestination?,
    modifier: Modifier = Modifier,
    floatingActionButton: (@Composable () -> Unit)? = null,
) {
    val localToolbarScrollableState = LocalToolbarScrollableState.current

    CalendarHorizontalFloatingToolbar(
        onNavigationButtonClicked = onNavigationButtonClicked,
        onCurrentDayClicked = onCurrentDayClicked,
        currentDestination = currentDestination,
        modifier = modifier,
        floatingActionButton = floatingActionButton,
        isExpanded = { localToolbarScrollableState?.isExpanded ?: true },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CalendarHorizontalFloatingToolbar(
    onNavigationButtonClicked: (NavDestination.PlageDateDestination) -> Unit,
    onCurrentDayClicked: () -> Unit,
    currentDestination: () -> NavDestination.PlageDateDestination?,
    modifier: Modifier = Modifier,
    floatingActionButton: (@Composable () -> Unit)? = null,
    isExpanded: () -> Boolean,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(NavigationBarDefaults.windowInsets)
            .sharedElement(key = FLOATING_TOOLBARBAR_KEY)
            .padding(end = Margin.Large),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (floatingActionButton != null) {
            HorizontalFloatingToolbar(
                expanded = isExpanded(),
                floatingActionButton = { floatingActionButton.invoke() },
                content = {
                    ContentFloatingToolbar(onCurrentDayClicked, onNavigationButtonClicked, currentDestination)
                },
            )
        } else {
            HorizontalFloatingToolbar(
                expanded = isExpanded(),
                content = {
                    ContentFloatingToolbar(onCurrentDayClicked, onNavigationButtonClicked, currentDestination)
                },
            )
        }
    }
}

@Composable
private fun ContentFloatingToolbar(
    onCurrentDayClicked: () -> Unit,
    onNavigationButtonClicked: (NavDestination.PlageDateDestination) -> Unit,
    currentDestination: () -> NavDestination.PlageDateDestination?,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        IconButton(onClick = onCurrentDayClicked) {
            Icon(imageVector = Icons.Outlined.CalendarToday, contentDescription = null)
        }
        DropdownIconButton(
            onNavigationButtonClicked = onNavigationButtonClicked,
            currentDestination = currentDestination,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DropdownIconButton(
    onNavigationButtonClicked: (NavDestination.PlageDateDestination) -> Unit,
    currentDestination: () -> NavDestination.PlageDateDestination?,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
) {
    var menuExpanded by remember { mutableStateOf(isExpanded) }

    Box(modifier = modifier) {
        currentDestination()?.let {
            IconButton(onClick = { menuExpanded = !menuExpanded }) {
                Icon(imageVector = getSelectedIcon(lastMainNavigationSelected = it), contentDescription = null)
            }
        }

        DropdownMenuPopup(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            popupPositionProvider = MenuDefaults.rememberDropdownMenuPopupPositionProvider(
                dropdownMenuAnchorPosition = MenuAnchorPosition.Above,
            ),
            modifier = Modifier.padding(bottom = Margin.Medium),
            properties = PopupProperties(clippingEnabled = false, focusable = true),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MenuDefaults.containerColor,
                tonalElevation = MenuDefaults.TonalElevation,
            ) {
                Column(modifier = Modifier) {
                    DateSelectionItems.entries.forEachIndexed { index, item ->
                        DropdownMenuItem(
                            shape = MenuDefaults.itemShape(index, DateSelectionItems.entries.count()).shape,
                            text = { Text(item.label) },
                            leadingIcon = { Icon(imageVector = item.icon, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onNavigationButtonClicked(item.destination)
                            },
                            modifier = Modifier.widthIn(min = 180.dp),
                        )
                    }
                }
            }
        }
    }
}

enum class DateSelectionItems(val label: String, val icon: ImageVector, val destination: NavDestination.PlageDateDestination) {
    Day(label = "Day", icon = Icons.Outlined.ViewDay, destination = NavDestination.PlageDateDestination.Day),
    ThreeDays(label = "Three days", icon = Icons.Outlined.ViewDay, destination = NavDestination.PlageDateDestination.ThreeDays),
    Week(label = "Week", icon = Icons.Outlined.ViewWeek, destination = NavDestination.PlageDateDestination.Week),
    Month(label = "Month", icon = Icons.Outlined.CalendarMonth, destination = NavDestination.PlageDateDestination.Month),
    Planning(label = "Planning", icon = Icons.Outlined.ViewAgenda, destination = NavDestination.PlageDateDestination.Planning)
}

@Composable
private fun getSelectedIcon(lastMainNavigationSelected: NavDestination.PlageDateDestination): ImageVector {
    return when (lastMainNavigationSelected) {
        is NavDestination.PlageDateDestination.Day -> Icons.Outlined.ViewDay
        is NavDestination.PlageDateDestination.ThreeDays -> Icons.Outlined.ViewDay
        is NavDestination.PlageDateDestination.Week -> Icons.Outlined.ViewWeek
        is NavDestination.PlageDateDestination.Month -> Icons.Outlined.CalendarMonth
        is NavDestination.PlageDateDestination.Planning -> Icons.Outlined.ViewAgenda
    }
}

@Preview
@Composable
private fun CalendarHorizontalFloatingToolbarPreview() {
    CalendarHorizontalFloatingToolbar(
        onNavigationButtonClicked = { },
        currentDestination = { NavDestination.PlageDateDestination.Week },
        floatingActionButton = { CalendarFab(onClick = { }) },
        onCurrentDayClicked = { },
    )
}
