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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.infomaniak.calendar.extensions.appGraph
import com.infomaniak.calendar.manager.SyncEventsManager
import com.infomaniak.calendar.ui.LocalUser
import com.infomaniak.calendar.ui.navigation.MainNavHost
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.navigation.replaceRoot
import com.infomaniak.calendar.ui.navigation.state.LocalDrawerState
import com.infomaniak.calendar.ui.navigation.state.LocalSharedSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.LocalToolbarScrollableState
import com.infomaniak.calendar.ui.navigation.state.rememberCustomSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.rememberToolbarScrollableState
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.state.VisibleDayState
import com.infomaniak.calendar.ui.state.rememberVisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarTheme
import com.infomaniak.calendar.utils.UserLoadState
import com.infomaniak.calendar.utils.rememberUserLoadState
import com.infomaniak.core.auth.models.user.User
import kotlinx.coroutines.channels.ReceiveChannel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = appGraph.metroViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (SDK_INT >= 29) window.isNavigationBarContrastEnforced = false

        setContent {
            CalendarTheme {
                Surface {
                    when (val userLoadState = appGraph.accountUtils.rememberUserLoadState().value) {
                        UserLoadState.Awaiting -> Unit // Blank surface while waiting for first result
                        is UserLoadState.Loaded -> MainContent(
                            userLoadState = userLoadState,
                            visibleDayState = rememberVisibleDayState(mainViewModel.visibleDay),
                            loadingEventsError = mainViewModel.loadingEventsError,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    userLoadState: UserLoadState.Loaded,
    visibleDayState: VisibleDayState,
    loadingEventsError: ReceiveChannel<SyncEventsManager.SyncError>,
) {
    val startDestination = if (userLoadState.user == null) NavDestination.Onboarding() else NavDestination.CalendarView.Planning
    val backStack = rememberNavBackStack(startDestination)

    CompositionLocalProvider(
        LocalUser provides userLoadState.user,
        LocalVisibleDayState provides visibleDayState,
        LocalSharedSnackbarHostState provides rememberCustomSnackbarHostState(),
        LocalToolbarScrollableState provides rememberToolbarScrollableState(),
        LocalDrawerState provides rememberDrawerState(initialValue = DrawerValue.Closed),
    ) {
        ObserveSyncError(loadingEventsError)
        NavigateToOnboardingIfLastUserIsDisconnected(backStack, userLoadState.user)
        MainNavHost(backStack)
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
private fun NavigateToOnboardingIfLastUserIsDisconnected(backStack: NavBackStack<NavKey>, user: User?) {
    val isUserLoaded = user != null

    LaunchedEffect(isUserLoaded) {
        if (isUserLoaded.not()) {
            backStack.replaceRoot(NavDestination.Onboarding())
        }
    }
}
