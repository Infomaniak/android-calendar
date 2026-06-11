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

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.infomaniak.calendar.ui.screen.calendarTest.calendarTest
import com.infomaniak.calendar.ui.screen.home.HomeScreen
import com.infomaniak.calendar.ui.screen.onboarding.OnboardingScreen

@Composable
fun MainNavHost(
    backStack: NavBackStack<NavKey>,
) {
    NavDisplay(backStack = backStack, entryProvider = baseEntryProvider(backStack))
}

private fun baseEntryProvider(backStack: NavBackStack<NavKey>): (NavKey) -> NavEntry<NavKey> = entryProvider {
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
    entry<NavDestination.Home> {
        HomeScreen()
    }
    calendarTest()
}
