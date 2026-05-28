package com.infomaniak.calendar.ui.component.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionLayout
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.navigation.addOrMoveToTop

private const val NAVIGATION_BAR_KEY = "NAVIGATION_BAR"

@Composable
fun CalendarNavigationBar(
    backStack: NavBackStack<NavKey>,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope?,
    modifier: Modifier = Modifier,
) {
    val currentKey: NavKey = backStack.last()

    NavigationBar(modifier = modifier.sharedNavigation(NAVIGATION_BAR_KEY, sharedTransitionScope, animatedContentScope)) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavigationBarItem(
                selected = currentKey is NavDestination.Planning,
                icon = { Icon(imageVector = Icons.Filled.ViewAgenda, contentDescription = null) },
                label = { Text("Planning") },
                onClick = { backStack.addOrMoveToTop(NavDestination.Planning) },
            )
            NavigationBarItem(
                selected = currentKey is NavDestination.Day,
                icon = { Icon(imageVector = Icons.Filled.ViewDay, contentDescription = null) },
                label = { Text("Day") },
                onClick = { backStack.addOrMoveToTop(NavDestination.Day) },
            )
            NavigationBarItem(
                selected = currentKey is NavDestination.Week,
                icon = { Icon(imageVector = Icons.Filled.ViewWeek, contentDescription = null) },
                label = { Text("Week") },
                onClick = { backStack.addOrMoveToTop(NavDestination.Week) },
            )
            NavigationBarItem(
                selected = currentKey is NavDestination.Month,
                icon = { Icon(imageVector = Icons.Filled.CalendarViewMonth, contentDescription = null) },
                label = { Text("Month") },
                onClick = { backStack.addOrMoveToTop(NavDestination.Month) },
            )
        }
    }
}

@Preview
@Composable
private fun CalendarNavigationBarPreview() {
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(NavDestination.Week)

    SharedTransitionLayout {
        CalendarNavigationBar(
            backStack = backStack,
            sharedTransitionScope = this@SharedTransitionLayout,
            animatedContentScope = null,
        )
    }
}
