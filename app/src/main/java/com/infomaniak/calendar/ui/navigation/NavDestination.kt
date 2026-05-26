package com.infomaniak.calendar.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavDestination : NavKey {

    @Serializable
    data object Home : NavDestination
}
