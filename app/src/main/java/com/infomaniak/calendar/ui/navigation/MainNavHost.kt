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
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.ui.NavDisplay
import com.infomaniak.calendar.ui.component.CalendarFab
import com.infomaniak.calendar.ui.modifier.LocalSharedTransitionScope
import com.infomaniak.calendar.ui.navigation.component.CalendarHorizontalFloatingToolbar
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.MetadataSceneStrategy.FloatingToolbarWithFab
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.NavigationDecoratorStrategy
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.metaDataOf
import com.infomaniak.calendar.ui.navigation.state.LocalSharedSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.LocalToolbarScrollableState
import com.infomaniak.calendar.ui.navigation.state.SharedSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.ToolbarScrollableState
import com.infomaniak.calendar.ui.navigation.state.rememberCustomSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.rememberToolbarScrollableState
import com.infomaniak.calendar.ui.screen.agenda.PlanningScreen
import com.infomaniak.calendar.ui.screen.calendarTest.calendarTest
import com.infomaniak.calendar.ui.screen.day.DayScreen
import com.infomaniak.calendar.ui.screen.eventCreation.EventCreationScreen
import com.infomaniak.calendar.ui.screen.month.MonthScreen
import com.infomaniak.calendar.ui.screen.onboarding.OnboardingScreen
import com.infomaniak.calendar.ui.screen.threeDays.ThreeDayScreen
import com.infomaniak.calendar.ui.screen.week.WeekScreen

@Composable
fun MainNavHost(backStack: NavBackStack<NavKey>) {
    val snackbarHostState: SharedSnackbarHostState = rememberCustomSnackbarHostState()
    val toolbarScrollableState: ToolbarScrollableState = rememberToolbarScrollableState()

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this@SharedTransitionLayout,
            LocalSharedSnackbarHostState provides snackbarHostState,
            LocalToolbarScrollableState provides toolbarScrollableState,
        ) {
            NavDisplay(
                backStack = backStack,
                entryProvider = baseEntryProvider(backStack = backStack),
                sceneDecoratorStrategies = sceneDecoratorStrategies(backStack = backStack),
                sharedTransitionScope = this@SharedTransitionLayout,
            )
        }
    }
}

private fun baseEntryProvider(backStack: NavBackStack<NavKey>): (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<NavDestination.CalendarView.Planning>(metadata = metaDataOf(FloatingToolbarWithFab)) {
        PlanningScreen()
    }
    entry<NavDestination.CalendarView.Day>(metadata = metaDataOf(FloatingToolbarWithFab)) {
        DayScreen()
    }
    entry<NavDestination.CalendarView.ThreeDays>(metadata = metaDataOf(FloatingToolbarWithFab)) {
        ThreeDayScreen()
    }
    entry<NavDestination.CalendarView.Week>(metadata = metaDataOf(FloatingToolbarWithFab)) {
        WeekScreen()
    }
    entry<NavDestination.CalendarView.Month>(metadata = metaDataOf(FloatingToolbarWithFab)) {
        MonthScreen()
    }
    entry<NavDestination.EventCreation> {
        EventCreationScreen()
    }
    entry<NavDestination.Onboarding> { destination ->
        OnboardingScreen(
            onlyLogin = destination.onlyLogin,
            goToCalendarView = {
                backStack.clear()
                backStack.add(NavDestination.CalendarView.Day)
            },
            onPopBack = { backStack.removeLastOrNull() },
        )
    }
    calendarTest()
}

@Composable
private fun sceneDecoratorStrategies(backStack: NavBackStack<NavKey>): List<SceneDecoratorStrategy<NavKey>> {
    val navigationStrategy: NavigationDecoratorStrategy<NavKey> = remember {
        NavigationDecoratorStrategy(
            floatingToolbar = {
                CalendarHorizontalFloatingToolbar(
                    onNavigationButtonClicked = { backStack.addOrMoveToTop(it) },
                    onCurrentDayClicked = { },
                    currentDestination = { backStack.getLastCalendarView() },
                    floatingActionButton = {
                        CalendarFab(
                            onClick = { backStack.addOrMoveToTop(NavDestination.EventCreation) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
                )
            },
        )
    }

    return listOf(navigationStrategy)
}

private fun NavBackStack<NavKey>.getLastCalendarView(): NavDestination.CalendarView? {
    return this.filterIsInstance<NavDestination.CalendarView>().lastOrNull()
}

private fun NavBackStack<NavKey>.addOrMoveToTop(destination: NavKey) {
    this.removeAll { it == destination }
    this.add(destination)
}
