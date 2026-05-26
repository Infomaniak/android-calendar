package com.infomaniak.calendar.ui.component

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.navigation.addOrMoveToTop

private const val NAVIGATION_BAR_KEY = "NAVIGATION_BAR"

@Composable
fun CalendarBottomBar(
    backStack: NavBackStack<NavKey>,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
) {
    val animatedContentScope = LocalNavAnimatedContentScope.current

    with(sharedTransitionScope) {
        NavigationBar(
            modifier = modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = NAVIGATION_BAR_KEY),
                animatedVisibilityScope = animatedContentScope,
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavigationBarItem(
                    selected = backStack.last() is NavDestination.Planning,
                    icon = { Icon(imageVector = Icons.Filled.ViewAgenda, contentDescription = null) },
                    label = { Text("Planning") },
                    onClick = { backStack.addOrMoveToTop(NavDestination.Planning) },
                )
                NavigationBarItem(
                    selected = backStack.last() is NavDestination.Day,
                    icon = { Icon(imageVector = Icons.Filled.ViewDay, contentDescription = null) },
                    label = { Text("Day") },
                    onClick = { backStack.addOrMoveToTop(NavDestination.Day) },
                )
                NavigationBarItem(
                    selected = backStack.last() is NavDestination.Week,
                    icon = { Icon(imageVector = Icons.Filled.ViewWeek, contentDescription = null) },
                    label = { Text("Week") },
                    onClick = { backStack.addOrMoveToTop(NavDestination.Week) },
                )
                NavigationBarItem(
                    selected = backStack.last() is NavDestination.Month,
                    icon = { Icon(imageVector = Icons.Filled.CalendarViewMonth, contentDescription = null) },
                    label = { Text("Month") },
                    onClick = { backStack.addOrMoveToTop(NavDestination.Month) },
                )
            }
        }
    }
}
