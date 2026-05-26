package com.infomaniak.calendar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.infomaniak.calendar.ui.screen.home.HomeScreen

@Composable
fun MainNavHost(startDestination: NavDestination) {
    val backStack = rememberNavBackStack(startDestination)
    NavDisplay(backStack = backStack, entryProvider = baseEntryProvider())
}

private fun baseEntryProvider(): (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<NavDestination.Home> {
        HomeScreen()
    }
}
