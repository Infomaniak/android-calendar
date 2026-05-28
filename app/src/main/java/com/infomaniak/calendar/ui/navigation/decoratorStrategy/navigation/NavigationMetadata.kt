package com.infomaniak.calendar.ui.navigation.decoratorStrategy.navigation

object NavigationMetadata {
    fun showNavigation(): Map<String, Any> = mapOf(SHOULD_SHOW_NAVIGATION_KEY to true)

    const val SHOULD_SHOW_NAVIGATION_KEY = "ShouldShowNavigationKey"
}
