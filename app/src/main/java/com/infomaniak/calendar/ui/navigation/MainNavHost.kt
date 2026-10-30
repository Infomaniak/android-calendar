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
import androidx.compose.ui.platform.LocalContext
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
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.NavigationMetadata
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.ResponsiveDialogSceneStrategy
import com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation.metaDataOf
import com.infomaniak.calendar.ui.screen.accounts.AccountActionsScreen
import com.infomaniak.calendar.ui.screen.accounts.AccountsListScreen
import com.infomaniak.calendar.ui.screen.day.DayScreen
import com.infomaniak.calendar.ui.screen.eventDetail.attendeesSearch.AttendeesSearchScreen
import com.infomaniak.calendar.ui.screen.eventDetail.creation.EventCreationScreen
import com.infomaniak.calendar.ui.screen.eventDetail.customRecurrence.CustomRecurrenceScreen
import com.infomaniak.calendar.ui.screen.eventDetail.customReminder.CustomReminderScreen
import com.infomaniak.calendar.ui.screen.eventDetail.detail.EventDetailScreen
import com.infomaniak.calendar.ui.screen.eventDetail.edit.EventEditScreen
import com.infomaniak.calendar.ui.screen.eventDetail.timeZoneSearch.TimeZoneSearchScreen
import com.infomaniak.calendar.ui.screen.month.MonthScreen
import com.infomaniak.calendar.ui.screen.multiDays.threeDays.ThreeDayScreen
import com.infomaniak.calendar.ui.screen.multiDays.week.WeekScreen
import com.infomaniak.calendar.ui.screen.onboarding.login.LoginOnboardingScreen
import com.infomaniak.calendar.ui.screen.onboarding.permissions.PermissionsOnboardingPage
import com.infomaniak.calendar.ui.screen.onboarding.permissions.PermissionsOnboardingScreen
import com.infomaniak.calendar.ui.screen.planning.PlanningScreen
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.utils.NavigationTransition
import com.infomaniak.core.common.utils.today
import kotlin.time.Clock

@Composable
fun MainNavHost(
    backStack: NavBackStack<NavDestination>,
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
    backStack: NavBackStack<NavDestination>,
    defaultCalendarView: NavDestination.CalendarView,
): (NavDestination) -> NavEntry<NavDestination> = { destination ->
    when (destination) {
        is NavDestination.CalendarView.Planning -> NavEntry(destination, metadata = metaDataOf(FloatingToolbarWithFab, Drawer)) {
            PlanningScreen(
                goToEventCreation = { backStack.addOnce(NavDestination.EventCreation(it)) },
                goToEventDetail = { backStack.addOnce(NavDestination.EventDetail(it)) },
            )
        }

        is NavDestination.CalendarView.Day -> NavEntry(destination, metadata = metaDataOf(FloatingToolbarWithFab, Drawer)) {
            DayScreen(goToEventDetail = { backStack.addOnce(NavDestination.EventDetail(it)) })
        }

        is NavDestination.CalendarView.ThreeDays -> NavEntry(destination, metadata = metaDataOf(FloatingToolbarWithFab, Drawer)) {
            ThreeDayScreen(goToEventDetail = { backStack.addOnce(NavDestination.EventDetail(it)) })
        }

        is NavDestination.CalendarView.Week -> NavEntry(destination, metadata = metaDataOf(FloatingToolbarWithFab, Drawer)) {
            WeekScreen(goToEventDetail = { backStack.addOnce(NavDestination.EventDetail(it)) })
        }

        is NavDestination.CalendarView.Month -> NavEntry(destination, metadata = metaDataOf(FloatingToolbarWithFab, Drawer)) {
            MonthScreen()
        }

        is NavDestination.EventCreation -> NavEntry(
            key = destination,
            contentKey = destination.toContentKey(),
            metadata = metaDataOf(ResponsiveDialog),
        ) {
            EventCreationScreen(
                date = destination.date,
                goBack = { backStack.popOrReplaceRoot(defaultCalendarView) },
                goToEventsAttendees = { backStack.addOnce(NavDestination.EventAttendees(parent = destination)) },
                goToTimeZoneSearch = { isStart -> backStack.addOnce(NavDestination.EventTimeZone(destination, isStart)) },
                goToCustomReminder = { backStack.addOnce(NavDestination.EventCustomReminder(destination)) },
                goToCustomRecurrence = { start -> backStack.addOnce(NavDestination.EventCustomRecurrence(destination, start)) },
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
            metadata = metaDataOf(
                ResponsiveDialog,
                shareViewModelStoreWith(NavDestination.EventDetail(destination.occurrenceId)),
            ),
        ) {
            EventEditScreen(
                occurrenceId = destination.occurrenceId,
                goBack = { backStack.popOrReplaceRoot(defaultCalendarView) },
                goBackToCalendarView = { backStack.popUntilCalendarView(defaultCalendarView) },
                goToEventAttendees = { backStack.addOnce(NavDestination.EventAttendees(parent = destination)) },
                goToTimeZoneSearch = { isStart -> backStack.addOnce(NavDestination.EventTimeZone(destination, isStart)) },
                goToCustomReminder = { backStack.addOnce(NavDestination.EventCustomReminder(destination)) },
                goToCustomRecurrence = { start -> backStack.addOnce(NavDestination.EventCustomRecurrence(destination, start)) },
            )
        }

        is NavDestination.EventAttendees -> NavEntry(
            key = destination,
            metadata = metaDataOf(ResponsiveDialog, shareViewModelStoreWith(destination.parent)),
        ) {
            AttendeesSearchScreen(
                goBack = { backStack.popOrReplaceRoot(defaultCalendarView) },
            )
        }

        is NavDestination.EventTimeZone -> NavEntry(
            key = destination,
            metadata = metaDataOf(ResponsiveDialog, shareViewModelStoreWith(destination.parent)),
        ) {
            TimeZoneSearchScreen(
                isStart = destination.isStart,
                goBack = { backStack.popOrReplaceRoot(defaultCalendarView) },
            )
        }

        is NavDestination.EventCustomReminder -> NavEntry(
            key = destination,
            metadata = metaDataOf(ResponsiveDialog, shareViewModelStoreWith(destination.parent)),
        ) {
            CustomReminderScreen(goBack = { backStack.popOrReplaceRoot(defaultCalendarView) })
        }

        is NavDestination.EventCustomRecurrence -> NavEntry(
            key = destination,
            metadata = metaDataOf(ResponsiveDialog, shareViewModelStoreWith(destination.parent)),
        ) {
            CustomRecurrenceScreen(start = destination.start, goBack = { backStack.popOrReplaceRoot(defaultCalendarView) })
        }

        is NavDestination.Accounts.List -> NavEntry(destination) {
            AccountsListScreen(
                onBack = { backStack.popOrReplaceRoot(backStack.getLastCalendarView() ?: defaultCalendarView) },
                onAddAccount = { backStack.addOnce(NavDestination.Onboarding.Login(onlyLogin = true)) },
                onAccountClick = { userId -> backStack.addOnce(NavDestination.Accounts.Actions(userId)) },
            )
        }

        is NavDestination.Accounts.Actions -> NavEntry(destination) {
            AccountActionsScreen(
                userId = destination.userId,
                onBack = { backStack.popOrReplaceRoot(NavDestination.Accounts.List) },
            )
        }

        is NavDestination.Onboarding.Login -> NavEntry(destination) {
            val context = LocalContext.current

            LoginOnboardingScreen(
                onlyLogin = destination.onlyLogin,
                onLoggedIn = {
                    val isPermissionMissing = PermissionsOnboardingPage.isAnyPermissionMissing(context)
                    backStack.replaceRoot(if (isPermissionMissing) NavDestination.Onboarding.Permissions else defaultCalendarView)
                },
                onPopBack = { backStack.popOrReplaceRoot(backStack.getLastCalendarView() ?: defaultCalendarView) },
            )
        }

        is NavDestination.Onboarding.Permissions -> NavEntry(destination) {
            PermissionsOnboardingScreen(goToCalendarView = { backStack.replaceRoot(defaultCalendarView) })
        }
    }
}

@Composable
private fun <T : NavKey> sceneStrategies(windowSizeClass: WindowSizeClass): List<SceneStrategy<T>> {
    // ResponsiveDialogSceneStrategy holds the scene of the ongoing dialog, it must survive recompositions.
    return remember(windowSizeClass) { listOf(ResponsiveDialogSceneStrategy(windowSizeClass)) }
}

private fun sceneDecoratorStrategies(
    backStack: NavBackStack<NavDestination>,
    onCalendarViewSelected: (NavDestination.CalendarView) -> Unit,
): List<SceneDecoratorStrategy<NavDestination>> {
    val navigationStrategy: NavigationDecoratorStrategy<NavDestination> =
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
                            onClick = {
                                backStack.addOnce(NavDestination.EventCreation(visibleDayState?.visibleDate ?: Clock.today()))
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
                )
            },
        )

    val drawerStrategy = DrawerDecoratorStrategy<NavDestination>(
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

/**
 * Shares [destination]'s ViewModelStore with this entry. Its ViewModels are cleared when [destination] is popped.
 */
private fun shareViewModelStoreWith(destination: NavDestination): NavigationMetadata = object : NavigationMetadata {
    override val metadata: Map<String, Any> = SharedViewModelStoreNavEntryDecorator.parent(destination.toContentKey())
}

private fun NavBackStack<NavDestination>.getLastCalendarView(): NavDestination.CalendarView? {
    return this.filterIsInstance<NavDestination.CalendarView>().lastOrNull()
}
