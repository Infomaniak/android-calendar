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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.infomaniak.calendar.ui.component.CalendarFab
import com.infomaniak.calendar.ui.component.drawer.MenuDrawer
import com.infomaniak.calendar.ui.modifier.LocalSharedTransitionScope
import com.infomaniak.calendar.ui.navigation.component.CalendarHorizontalFloatingToolbar
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.DrawerDecoratorStrategy
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.MetadataSceneStrategy.Drawer
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.MetadataSceneStrategy.FloatingToolbarWithFab
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.MetadataSceneStrategy.ResponsiveDialog
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.NavigationDecoratorStrategy
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.ResponsiveDialogSceneStrategy
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.metaDataOf
import com.infomaniak.calendar.ui.screen.accounts.AccountActionsScreen
import com.infomaniak.calendar.ui.screen.accounts.AccountsListScreen
import com.infomaniak.calendar.ui.screen.attendeesSearch.EventAttendeesScreen
import com.infomaniak.calendar.ui.screen.day.DayScreen
import com.infomaniak.calendar.ui.screen.eventDetail.creation.EventCreationScreen
import com.infomaniak.calendar.ui.screen.eventDetail.detail.EventDetailScreen
import com.infomaniak.calendar.ui.screen.eventDetail.edit.EventEditScreen
import com.infomaniak.calendar.ui.screen.month.MonthScreen
import com.infomaniak.calendar.ui.screen.onboarding.OnboardingScreen
import com.infomaniak.calendar.ui.screen.planning.PlanningScreen
import com.infomaniak.calendar.ui.screen.threeDays.ThreeDayScreen
import com.infomaniak.calendar.ui.screen.week.WeekScreen
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.core.common.utils.today
import kotlin.time.Clock

@Composable
fun MainNavHost(
    backStack: NavBackStack<NavKey>,
    defaultCalendarView: NavDestination.CalendarView,
    onCalendarViewSelected: (NavDestination.CalendarView) -> Unit,
) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout) {
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

            NavDisplay(
                backStack = backStack,
                entryProvider = baseEntryProvider(backStack, defaultCalendarView),
                sceneDecoratorStrategies = sceneDecoratorStrategies(backStack, onCalendarViewSelected),
                sceneStrategies = sceneStrategies(windowSizeClass),
                sharedTransitionScope = this@SharedTransitionLayout,
            )
        }
    }
}

private fun baseEntryProvider(
    backStack: NavBackStack<NavKey>,
    defaultCalendarView: NavDestination.CalendarView,
): (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<NavDestination.CalendarView.Planning>(metadata = metaDataOf(FloatingToolbarWithFab, Drawer)) {
        PlanningScreen(
            goToEventCreation = { backStack.addOnce(NavDestination.EventCreation) },
            goToEventDetail = { backStack.addOnce(NavDestination.EventDetail(it)) },
        )
    }
    entry<NavDestination.CalendarView.Day>(metadata = metaDataOf(FloatingToolbarWithFab, Drawer)) {
        DayScreen(goToEventDetail = { backStack.addOnce(NavDestination.EventDetail(it)) })
    }
    entry<NavDestination.CalendarView.ThreeDays>(metadata = metaDataOf(FloatingToolbarWithFab, Drawer)) {
        ThreeDayScreen()
    }
    entry<NavDestination.CalendarView.Week>(metadata = metaDataOf(FloatingToolbarWithFab, Drawer)) {
        WeekScreen()
    }
    entry<NavDestination.CalendarView.Month>(metadata = metaDataOf(FloatingToolbarWithFab, Drawer)) {
        MonthScreen()
    }
    entry<NavDestination.EventCreation>(metadata = metaDataOf(ResponsiveDialog)) {
        EventCreationScreen()
    }
    entry<NavDestination.EventDetail>(metadata = metaDataOf(ResponsiveDialog)) { destination ->
        EventDetailScreen(
            occurrenceId = destination.occurrenceId,
            goBack = { backStack.popOrReplaceRoot(defaultCalendarView) },
            goToEdit = { backStack.addOnce(NavDestination.EventEdit(destination.occurrenceId)) },
            goToEventAttendees = { backStack.add(NavDestination.EventAttendees(destination.occurrenceId)) },
        )
    }
    entry<NavDestination.EventEdit>(metadata = metaDataOf(ResponsiveDialog)) { destination ->
        EventEditScreen(
            occurrenceId = destination.occurrenceId,
            goBack = { backStack.popOrReplaceRoot(defaultCalendarView) },
        )
    }
    entry<NavDestination.EventAttendees> { destination ->
        EventAttendeesScreen(
            occurrenceId = destination.occurrenceId,
            goBack = { backStack.popOrReplaceRoot(defaultCalendarView) },
        )
    }
    entry<NavDestination.Accounts.List> {
        AccountsListScreen(
            onBack = { backStack.popOrReplaceRoot(backStack.getLastCalendarView() ?: defaultCalendarView) },
            onAddAccount = { backStack.addOnce(NavDestination.Onboarding(onlyLogin = true)) },
            onAccountClick = { userId -> backStack.addOnce(NavDestination.Accounts.Actions(userId)) },
        )
    }
    entry<NavDestination.Accounts.Actions> { destination ->
        AccountActionsScreen(
            userId = destination.userId,
            onBack = { backStack.popOrReplaceRoot(NavDestination.Accounts.List) },
        )
    }
    entry<NavDestination.Onboarding> { destination ->
        OnboardingScreen(
            onlyLogin = destination.onlyLogin,
            goToCalendarView = { backStack.replaceRoot(defaultCalendarView) },
            onPopBack = { backStack.popOrReplaceRoot(backStack.getLastCalendarView() ?: defaultCalendarView) },
        )
    }
}

private fun sceneStrategies(windowSizeClass: WindowSizeClass): List<SceneStrategy<NavKey>> {
    val dialogStrategy = ResponsiveDialogSceneStrategy<NavKey>(windowSizeClass)
    return listOf(dialogStrategy)
}

private fun sceneDecoratorStrategies(
    backStack: NavBackStack<NavKey>,
    onCalendarViewSelected: (NavDestination.CalendarView) -> Unit,
): List<SceneDecoratorStrategy<NavKey>> {
    val navigationStrategy: NavigationDecoratorStrategy<NavKey> =
        NavigationDecoratorStrategy(
            floatingToolbar = {
                val visibleDayState = LocalVisibleDayState.current

                CalendarHorizontalFloatingToolbar(
                    onNavigationButtonClicked = { destination ->
                        onCalendarViewSelected(destination)
                        backStack.replaceRoot(destination)
                    },
                    onCurrentDayClicked = { visibleDayState?.jumpTo(Clock.today()) },
                    currentDestination = { backStack.getLastCalendarView() },
                    floatingActionButton = {
                        CalendarFab(
                            onClick = { backStack.add(NavDestination.EventCreation) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
                )
            },
        )

    val drawerStrategy = DrawerDecoratorStrategy<NavKey>(
        drawer = { content ->
            MenuDrawer(
                content = content,
                onManageAccounts = {
                    backStack.add(NavDestination.Accounts.List)
                },
            )
        },
    )

    return listOf(navigationStrategy, drawerStrategy)
}

private fun NavBackStack<NavKey>.getLastCalendarView(): NavDestination.CalendarView? {
    return this.filterIsInstance<NavDestination.CalendarView>().lastOrNull()
}
