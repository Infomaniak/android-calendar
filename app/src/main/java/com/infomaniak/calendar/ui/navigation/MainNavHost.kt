package com.infomaniak.calendar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay

@Composable
fun MainNavHost(startDestination: NavDestination) {
    val backStack = rememberNavBackStack(startDestination)
    NavDisplay(backStack = backStack, entryProvider = baseEntryProvider())
}
