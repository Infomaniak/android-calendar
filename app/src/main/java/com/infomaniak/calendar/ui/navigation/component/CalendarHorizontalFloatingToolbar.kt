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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.component.CalendarFab
import com.infomaniak.calendar.ui.modifier.sharedElement
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.navigation.state.LocalToolbarScrollableState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin

private const val FLOATING_TOOLBARBAR_KEY = "FloatingToolbar"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalendarHorizontalFloatingToolbar(
    onNavigationButtonClicked: (NavDestination.CalendarView) -> Unit,
    onCurrentDayClicked: () -> Unit,
    currentDestination: () -> NavDestination.CalendarView?,
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
    onNavigationButtonClicked: (NavDestination.CalendarView) -> Unit,
    onCurrentDayClicked: () -> Unit,
    currentDestination: () -> NavDestination.CalendarView?,
    isExpanded: () -> Boolean,
    modifier: Modifier = Modifier,
    floatingActionButton: (@Composable () -> Unit)? = null,
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
    onNavigationButtonClicked: (NavDestination.CalendarView) -> Unit,
    currentDestination: () -> NavDestination.CalendarView?,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        IconButton(onClick = onCurrentDayClicked) {
            Icon(
                painter = painterResource(R.drawable.ic_calendar_number_one),
                contentDescription = stringResource(R.string.contentDescriptionToday),
            )
        }
        DropdownIconButton(
            onNavigationButtonClicked = onNavigationButtonClicked,
            currentDestination = currentDestination,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DropdownIconButton(
    onNavigationButtonClicked: (NavDestination.CalendarView) -> Unit,
    currentDestination: () -> NavDestination.CalendarView?,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
) {
    var menuExpanded by remember { mutableStateOf(isExpanded) }

    Box(modifier = modifier) {
        currentDestination()?.let { destination ->
            CurrentDestinationIcon(currentDestination = destination, onMenuExpanded = { menuExpanded = !menuExpanded })
        }

        FloatingToolbarDropdownMenu(
            isExpanded = menuExpanded,
            onMenuExpanded = { menuExpanded = it },
            onNavigationButtonClicked = onNavigationButtonClicked,
        )
    }
}

@Preview
@Composable
private fun CalendarHorizontalFloatingToolbarPreview() = CalendarThemeForPreview {
    CalendarHorizontalFloatingToolbar(
        onNavigationButtonClicked = {},
        onCurrentDayClicked = {},
        currentDestination = { NavDestination.CalendarView.Week },
        floatingActionButton = { CalendarFab(onClick = {}) },
        isExpanded = { true },
    )
}
