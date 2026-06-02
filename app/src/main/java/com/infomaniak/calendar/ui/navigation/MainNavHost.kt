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
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalMediaQueryApi
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.ui.NavDisplay
import com.infomaniak.calendar.ui.component.CalendarFab
import com.infomaniak.calendar.ui.navigation.component.CalendarHorizontalFloatingToolbar
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.MetadataSceneStrategy.Fab
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.MetadataSceneStrategy.NavigationBar
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.NavigationDecoratorStrategy
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.metaDataOf
import com.infomaniak.calendar.ui.screen.day.DayScreen
import com.infomaniak.calendar.ui.screen.month.MonthScreen
import com.infomaniak.calendar.ui.screen.onboarding.OnboardingScreen
import com.infomaniak.calendar.ui.screen.planning.PlanningScreen
import com.infomaniak.calendar.ui.screen.subDestinationTest.SubDestinationScreen
import com.infomaniak.calendar.ui.screen.week.WeekScreen

val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalGlobalSnackbar = staticCompositionLocalOf<SnackbarHostState> { error("No SnackbarHostState provided") }

@OptIn(ExperimentalMediaQueryApi::class)
@Composable
fun MainNavHost(navBackStack: NavBackStack<NavKey>) {
    val snackbarHostState = remember { SnackbarHostState() }

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this@SharedTransitionLayout,
            LocalGlobalSnackbar provides snackbarHostState,
        ) {
            NavDisplay(
                backStack = navBackStack,
                entryProvider = baseEntryProvider(backStack = { navBackStack }),
                sceneDecoratorStrategies = sceneDecoratorStrategies(backStack = { navBackStack }),
                sharedTransitionScope = this@SharedTransitionLayout,
            )
        }
    }
}

private fun baseEntryProvider(backStack: () -> NavBackStack<NavKey>): (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<NavDestination.PlageDateDestination.Agenda>(metadata = metaDataOf(NavigationBar, Fab)) {
        PlanningScreen(goToSubDestination = { backStack().add(NavDestination.SubDestination) })
    }
    entry<NavDestination.PlageDateDestination.Day>(metadata = metaDataOf(NavigationBar, Fab)) {
        DayScreen()
    }
    entry<NavDestination.PlageDateDestination.ThreeDays>(metadata = metaDataOf(NavigationBar, Fab)) {
        DayScreen()
    }
    entry<NavDestination.PlageDateDestination.Week>(metadata = metaDataOf(NavigationBar, Fab)) {
        WeekScreen()
    }
    entry<NavDestination.PlageDateDestination.Month>(metadata = metaDataOf(NavigationBar, Fab)) {
        MonthScreen()
    }
    entry<NavDestination.SubDestination>(metadata = metaDataOf(Fab)) {
        SubDestinationScreen()
    }
    entry<NavDestination.EventCreation> {
        SubDestinationScreen()
    }
    entry<NavDestination.Onboarding> { destination ->
        OnboardingScreen(
            onlyLogin = destination.onlyLogin,
            onNavigateToHome = {
                backStack().clear()
                backStack().add(NavDestination.PlageDateDestination.Day)
            },
            onPopBack = { backStack().removeLastOrNull() },
        )
    }
}

@Composable
private fun sceneDecoratorStrategies(backStack: () -> NavBackStack<NavKey>): List<SceneDecoratorStrategy<NavKey>> {
    val navigationStrategy: NavigationDecoratorStrategy<NavKey> = remember(backStack) {
        NavigationDecoratorStrategy(
            floatingToolbar = { floatingActionButton ->
                CalendarHorizontalFloatingToolbar(
                    lastMainNavigationSelected = { backStack().filterIsInstance<NavDestination.PlageDateDestination>().last() },
                    onNavigationButtonClicked = { backStack().addOrMoveToTop(it) },
                    floatingActionButton = floatingActionButton,
                )
            },
            floatingActionButton = {
                CalendarFab { backStack().addOrMoveToTop(NavDestination.EventCreation) }
            },
        )
    }

    return listOf(navigationStrategy)
}

fun NavBackStack<NavKey>.addOrMoveToTop(destination: NavKey) {
    if (this.contains(destination)) this.remove(destination)
    this.add(destination)
}
