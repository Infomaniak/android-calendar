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
package com.infomaniak.calendar

import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.runtime.serialization.NavKeySerializer
import com.infomaniak.calendar.components.foundation.state.VisibleDayState
import com.infomaniak.calendar.components.foundation.state.rememberVisibleDayState
import com.infomaniak.calendar.extensions.appGraph
import com.infomaniak.calendar.manager.SyncEventsManager
import com.infomaniak.calendar.ui.navigation.MainNavHost
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.navigation.replaceRoot
import com.infomaniak.calendar.ui.navigation.state.LocalDrawerState
import com.infomaniak.calendar.ui.navigation.state.LocalSharedSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.LocalToolbarScrollableState
import com.infomaniak.calendar.ui.navigation.state.rememberCustomSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.rememberToolbarScrollableState
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarTheme
import kotlinx.coroutines.channels.ReceiveChannel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = appGraph.metroViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { mainViewModel.uiState.value == MainUiState.Loading }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (SDK_INT >= 29) window.isNavigationBarContrastEnforced = false

        setContent {
            val currentUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            CalendarTheme {
                Surface {
                    when (val uiState = currentUiState) {
                        MainUiState.Loading -> Unit // Covered by the splash screen
                        is MainUiState.Loaded -> MainContent(
                            uiState = uiState,
                            visibleDayState = rememberVisibleDayState(mainViewModel.visibleDate),
                            loadingEventsError = mainViewModel.loadingEventsError,
                            onCalendarViewSelected = mainViewModel::saveCalendarView,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    uiState: MainUiState.Loaded,
    visibleDayState: VisibleDayState,
    loadingEventsError: ReceiveChannel<SyncEventsManager.SyncError>,
    onCalendarViewSelected: (NavDestination.CalendarView) -> Unit,
) {
    val startDestination = if (uiState.isUserConnected) uiState.lastCalendarView else NavDestination.Onboarding()
    val backStack = rememberNavDestinationBackStack(startDestination)

    CompositionLocalProvider(
        LocalVisibleDayState provides visibleDayState,
        LocalSharedSnackbarHostState provides rememberCustomSnackbarHostState(),
        LocalToolbarScrollableState provides rememberToolbarScrollableState(),
        LocalDrawerState provides rememberDrawerState(initialValue = DrawerValue.Closed),
    ) {
        ObserveSyncError(loadingEventsError)
        NavigateToOnboardingIfLastUserIsDisconnected(backStack, isUserLoaded = uiState.isUserConnected)
        MainNavHost(
            backStack = backStack,
            defaultCalendarView = uiState.lastCalendarView,
            onCalendarViewSelected = onCalendarViewSelected,
        )
    }
}

@Composable
private fun rememberNavDestinationBackStack(vararg elements: NavDestination): NavBackStack<NavDestination> {
    return rememberSerializable(
        serializer = NavBackStackSerializer(elementSerializer = NavKeySerializer()),
    ) {
        NavBackStack(*elements)
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun ObserveSyncError(loadingEventsError: ReceiveChannel<SyncEventsManager.SyncError>) {
    val snackbarHostState = LocalSharedSnackbarHostState.current ?: return
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        for (error in loadingEventsError) {
            snackbarHostState.showSnackbar(message = context.getString(error.errorRes))
        }
    }
}

@Composable
private fun NavigateToOnboardingIfLastUserIsDisconnected(backStack: NavBackStack<NavDestination>, isUserLoaded: Boolean) {
    LaunchedEffect(isUserLoaded) {
        if (isUserLoaded.not()) {
            backStack.replaceRoot(NavDestination.Onboarding())
        }
    }
}
