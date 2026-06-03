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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalMediaQueryApi
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.ui.NavDisplay
import com.infomaniak.calendar.ui.component.CalendarFab
import com.infomaniak.calendar.ui.navigation.component.CalendarHorizontalFloatingToolbar
import com.infomaniak.calendar.ui.modifier.LocalSharedTransitionScope
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.MetadataSceneStrategy.FloatingToolbarWithFab
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.NavigationDecoratorStrategy
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.metaDataOf
import com.infomaniak.calendar.ui.navigation.state.SharedSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.LocalSharedSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.LocalToolbarScrollableState
import com.infomaniak.calendar.ui.navigation.state.ToolbarScrollableState
import com.infomaniak.calendar.ui.navigation.state.rememberCustomSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.rememberToolbarScrollableState
import com.infomaniak.calendar.ui.screen.agenda.PlanningScreen
import com.infomaniak.calendar.ui.screen.day.DayScreen
import com.infomaniak.calendar.ui.screen.eventCreation.EventCreationScreen
import com.infomaniak.calendar.ui.screen.month.MonthScreen
import com.infomaniak.calendar.ui.screen.onboarding.OnboardingScreen
import com.infomaniak.calendar.ui.screen.planning.PlanningScreen
import com.infomaniak.calendar.ui.screen.subDestinationTest.SubDestinationScreen
import com.infomaniak.calendar.ui.screen.threeDays.ThreeDayScreen
import com.infomaniak.calendar.ui.screen.week.WeekScreen

@OptIn(ExperimentalMediaQueryApi::class)
@Composable
fun MainNavHost(navBackStack: NavBackStack<NavKey>) {
    val toolbarPermissionState: ToolbarScrollableState = rememberToolbarScrollableState()
    val snackbarHostState: SharedSnackbarHostState = rememberCustomSnackbarHostState()

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this@SharedTransitionLayout,
            LocalSharedSnackbarHostState provides snackbarHostState,
            LocalToolbarScrollableState provides toolbarPermissionState,
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

private fun baseEntryProvider(): (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<NavDestination.PlageDateDestination.Planning>(metadata = metaDataOf(FloatingToolbarWithFab)) {
        PlanningScreen()
    }
    entry<NavDestination.PlageDateDestination.Day>(metadata = metaDataOf(FloatingToolbarWithFab)) {
        DayScreen()
    }
    entry<NavDestination.PlageDateDestination.ThreeDays>(metadata = metaDataOf(FloatingToolbarWithFab)) {
        ThreeDayScreen()
    }
    entry<NavDestination.PlageDateDestination.Week>(metadata = metaDataOf(FloatingToolbarWithFab)) {
        WeekScreen()
    }
    entry<NavDestination.PlageDateDestination.Month>(metadata = metaDataOf(FloatingToolbarWithFab)) {
        MonthScreen()
    }
    entry<NavDestination.EventCreation> {
        EventCreationScreen()
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
            floatingToolbar = {
                CalendarHorizontalFloatingToolbar(
                    onNavigationButtonClicked = { backStack().addOrMoveToTop(it) },
                    onCurrentDayClicked = { },
                    currentDestination = { backStack().getPlageDateDestination() },
                    floatingActionButton = {
                        CalendarFab(modifier = Modifier.fillMaxSize()) { backStack().addOrMoveToTop(NavDestination.EventCreation) }
                    },
                )
            },
        )
    }

    return listOf(navigationStrategy)
}

private fun NavBackStack<NavKey>.getPlageDateDestination(): NavDestination.PlageDateDestination? {
    return this.filterIsInstance<NavDestination.PlageDateDestination>().lastOrNull()
}

fun NavBackStack<NavKey>.addOrMoveToTop(destination: NavKey) {
    if (this.contains(destination)) this.remove(destination)
    this.add(destination)
}
