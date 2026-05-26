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
package com.infomaniak.calendar.ui.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.infomaniak.calendar.ui.component.CalendarBottomBar
import com.infomaniak.calendar.ui.component.CalendarNavigationRail
import com.infomaniak.calendar.ui.screen.day.DayScreen
import com.infomaniak.calendar.ui.screen.month.MonthScreen
import com.infomaniak.calendar.ui.screen.onboarding.OnboardingScreen
import com.infomaniak.calendar.ui.screen.planning.PlanningScreen
import com.infomaniak.calendar.ui.screen.subDestinationTest.SubDestinationScreen
import com.infomaniak.calendar.ui.screen.week.WeekScreen

@Composable
fun MainNavHost(navBackStack: NavBackStack<NavKey>) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidthDp = with(density) { windowInfo.containerSize.width.toDp() }
    val isExpandedScreen = screenWidthDp >= 600.dp

    SharedTransitionLayout {
        val navigationStrategy = remember(isExpandedScreen, navBackStack) {
            NavigationDecoratorStrategy<NavKey>(
                isExpandedScreen = isExpandedScreen,
                navBarContent = {
                    CalendarBottomBar(backStack = navBackStack, sharedTransitionScope = this@SharedTransitionLayout)
                },
                navRailContent = {
                    CalendarNavigationRail(backStack = navBackStack, sharedTransitionScope = this@SharedTransitionLayout)
                },
            )
        }

        NavDisplay(
            backStack = navBackStack,
            entryProvider = baseEntryProvider(navBackStack),
            sceneDecoratorStrategies = listOf(navigationStrategy),
            sharedTransitionScope = this@SharedTransitionLayout,
        )
    }
}

private fun baseEntryProvider(backStack: NavBackStack<NavKey>): (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<NavDestination.Planning> { PlanningScreen(backStack) }
    entry<NavDestination.Day> { DayScreen() }
    entry<NavDestination.Week> { WeekScreen() }
    entry<NavDestination.Month> { MonthScreen() }
    entry<NavDestination.SubDestinationTest> { SubDestinationScreen() }
    entry<NavDestination.Onboarding> { destination ->
        OnboardingScreen(
            onlyLogin = destination.onlyLogin,
            onNavigateToHome = {
                backStack.clear()
                backStack.add(NavDestination.CalendarTest)
            },
            onPopBack = { backStack.removeLastOrNull() },
        )
    }
}

fun NavBackStack<NavKey>.addOrMoveToTop(destination: NavKey) {
    if (this.contains(destination)) this.remove(destination)
    this.add(destination)
}
