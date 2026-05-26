package com.infomaniak.calendar.ui.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.infomaniak.calendar.ui.screen.home.HomeScreen

fun baseEntryProvider(): (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<NavDestination.Home> {
        HomeScreen()
    }
}
