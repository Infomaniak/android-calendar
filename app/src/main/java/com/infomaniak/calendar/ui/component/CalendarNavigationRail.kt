package com.infomaniak.calendar.ui.component

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.navigation.addOrMoveToTop

private const val NAVIGATION_RAIL_KEY = "NAVIGATION_RAIL"

@Composable
fun CalendarNavigationRail(
    backStack: NavBackStack<NavKey>,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
) {
    val currentKey = backStack.last()
    val animatedContentScope = LocalNavAnimatedContentScope.current

    with(sharedTransitionScope) {
        NavigationRail(
            modifier = modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = NAVIGATION_RAIL_KEY),
                animatedVisibilityScope = animatedContentScope,
            )
        ) {
            NavigationRailItem(
                selected = currentKey is NavDestination.Planning,
                icon = { Icon(imageVector = Icons.Filled.ViewAgenda, contentDescription = null) },
                label = { Text("Planning") },
                onClick = { backStack.addOrMoveToTop(NavDestination.Planning) },
            )
            NavigationRailItem(
                selected = currentKey is NavDestination.Day,
                icon = { Icon(imageVector = Icons.Filled.ViewDay, contentDescription = null) },
                label = { Text("Day") },
                onClick = { backStack.addOrMoveToTop(NavDestination.Day) },
            )
            NavigationRailItem(
                selected = currentKey is NavDestination.Week,
                icon = { Icon(imageVector = Icons.Filled.ViewWeek, contentDescription = null) },
                label = { Text("Week") },
                onClick = { backStack.addOrMoveToTop(NavDestination.Week) },
            )
            NavigationRailItem(
                selected = currentKey is NavDestination.Month,
                icon = { Icon(imageVector = Icons.Filled.CalendarViewMonth, contentDescription = null) },
                label = { Text("Month") },
                onClick = { backStack.addOrMoveToTop(NavDestination.Month) },
            )
        }
    }
}
