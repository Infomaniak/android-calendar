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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
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
import com.infomaniak.calendar.ui.screen.day.DayScreen
import com.infomaniak.calendar.ui.screen.eventDetail.attendeesSearch.EventAttendeesScreen
import com.infomaniak.calendar.ui.screen.eventDetail.creation.EventCreationScreen
import com.infomaniak.calendar.ui.screen.eventDetail.detail.EventDetailScreen
import com.infomaniak.calendar.ui.screen.eventDetail.edit.EventEditScreen
import com.infomaniak.calendar.ui.screen.month.MonthScreen
import com.infomaniak.calendar.ui.screen.onboarding.OnboardingScreen
import com.infomaniak.calendar.ui.screen.planning.PlanningScreen
import com.infomaniak.calendar.ui.screen.threeDays.ThreeDayScreen
import com.infomaniak.calendar.ui.screen.week.WeekScreen
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.utils.NavigationTransition
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
            val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass

            NavDisplay(
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberSharedViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = baseEntryProvider(
                    backStack = backStack,
                    defaultCalendarView = defaultCalendarView,
                ),
                sceneDecoratorStrategies = sceneDecoratorStrategies(backStack, onCalendarViewSelected),
                sceneStrategies = sceneStrategies(windowSizeClass),
                sharedTransitionScope = this@SharedTransitionLayout,
                transitionSpec = { NavigationTransition.contentTransform },
                popTransitionSpec = { NavigationTransition.contentTransform },
            )
        }
    }
}

private fun baseEntryProvider(
    backStack: NavBackStack<NavKey>,
    defaultCalendarView: NavDestination.CalendarView,
): (NavKey) -> NavEntry<NavKey> = { key ->
    when (val destination = key as NavDestination) {
        is NavDestination.CalendarView.Planning -> NavEntry(
            key = destination,
            metadata = metaDataOf(FloatingToolbarWithFab, Drawer),
        ) {
            PlanningScreen(
                goToEventCreation = { backStack.addOnce(NavDestination.EventCreation) },
                goToEventDetail = { backStack.addOnce(NavDestination.EventDetail(it)) },
            )
        }
        is NavDestination.CalendarView.Day -> NavEntry(
            key = destination,
            metadata = metaDataOf(FloatingToolbarWithFab, Drawer),
        ) {
            DayScreen(goToEventDetail = { backStack.addOnce(NavDestination.EventDetail(it)) })
        }
        is NavDestination.CalendarView.ThreeDays -> NavEntry(
            key = destination,
            metadata = metaDataOf(FloatingToolbarWithFab, Drawer),
        ) {
            ThreeDayScreen()
        }
        is NavDestination.CalendarView.Week -> NavEntry(
            key = destination,
            metadata = metaDataOf(FloatingToolbarWithFab, Drawer),
        ) {
            WeekScreen()
        }
        is NavDestination.CalendarView.Month -> NavEntry(
            key = destination,
            metadata = metaDataOf(FloatingToolbarWithFab, Drawer),
        ) {
            MonthScreen()
        }
        is NavDestination.EventCreation -> NavEntry(
            key = destination,
            contentKey = destination.toContentKey(),
            metadata = metaDataOf(ResponsiveDialog),
        ) {
            EventCreationScreen(
                goBack = { backStack.popOrReplaceRoot(defaultCalendarView) },
                goToEventsAttendees = { backStack.addOnce(NavDestination.EventAttendees(parent = destination)) },
            )
        }
        is NavDestination.EventDetail -> NavEntry(
            key = destination,
            contentKey = destination.toContentKey(),
            metadata = metaDataOf(ResponsiveDialog),
        ) {
            EventDetailScreen(
                occurrenceId = destination.occurrenceId,
                goBack = { backStack.popOrReplaceRoot(defaultCalendarView) },
                goToEdit = { backStack.addOnce(NavDestination.EventEdit(destination.occurrenceId)) },
                goToEventAttendees = { backStack.addOnce(NavDestination.EventAttendees(parent = destination)) },
            )
        }
        is NavDestination.EventEdit -> NavEntry(
            key = destination,
            contentKey = destination.toContentKey(),
            metadata = metaDataOf(ResponsiveDialog),
        ) {
            EventEditScreen(
                occurrenceId = destination.occurrenceId,
                goBack = { backStack.popOrReplaceRoot(defaultCalendarView) },
                goToEventAttendees = { backStack.addOnce(NavDestination.EventAttendees(parent = destination)) },
            )
        }
        is NavDestination.EventAttendees -> NavEntry(
            key = destination,
            metadata = metaDataOf(ResponsiveDialog) + SharedViewModelStoreNavEntryDecorator.parent(destination.parent.toContentKey()),
        ) {
            EventAttendeesScreen(
                goBack = { backStack.popOrReplaceRoot(defaultCalendarView) },
            )
        }
        is NavDestination.Accounts.List -> NavEntry(
            key = destination,
        ) {
            AccountsListScreen(
                onBack = { backStack.popOrReplaceRoot(backStack.getLastCalendarView() ?: defaultCalendarView) },
                onAddAccount = { backStack.addOnce(NavDestination.Onboarding(onlyLogin = true)) },
                onAccountClick = { userId -> backStack.addOnce(NavDestination.Accounts.Actions(userId)) },
            )
        }
        is NavDestination.Accounts.Actions -> NavEntry(
            key = destination,
        ) {
            AccountActionsScreen(
                userId = destination.userId,
                onBack = { backStack.popOrReplaceRoot(NavDestination.Accounts.List) },
            )
        }
        is NavDestination.Onboarding -> NavEntry(
            key = destination,
        ) {
            OnboardingScreen(
                onlyLogin = destination.onlyLogin,
                goToCalendarView = { backStack.replaceRoot(defaultCalendarView) },
                onPopBack = { backStack.popOrReplaceRoot(backStack.getLastCalendarView() ?: defaultCalendarView) },
            )
        }
    }
}

@Composable
private fun sceneStrategies(windowSizeClass: WindowSizeClass): List<SceneStrategy<NavKey>> {
    // ResponsiveDialogSceneStrategy holds the scene of the ongoing dialog, it must survive recompositions.
    return remember(windowSizeClass) { listOf(ResponsiveDialogSceneStrategy(windowSizeClass)) }
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
                            onClick = { backStack.addOnce(NavDestination.EventCreation) },
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
                    backStack.addOnce(NavDestination.Accounts.List)
                },
            )
        },
    )

    return listOf(navigationStrategy, drawerStrategy)
}

private fun NavKey.toContentKey() = this.toString()

private fun NavBackStack<NavKey>.getLastCalendarView(): NavDestination.CalendarView? {
    return this.filterIsInstance<NavDestination.CalendarView>().lastOrNull()
}
