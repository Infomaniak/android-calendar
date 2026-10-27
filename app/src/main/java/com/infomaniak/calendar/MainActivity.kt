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
import android.content.Intent
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
import androidx.compose.runtime.mutableStateOf
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
import com.infomaniak.calendar.notification.NotificationHelper
import com.infomaniak.calendar.ui.navigation.MainNavHost
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.navigation.addOnce
import com.infomaniak.calendar.ui.navigation.replaceRoot
import com.infomaniak.calendar.ui.navigation.state.LocalDrawerState
import com.infomaniak.calendar.ui.navigation.state.LocalSharedSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.LocalToolbarScrollableState
import com.infomaniak.calendar.ui.navigation.state.rememberCustomSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.rememberToolbarScrollableState
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarTheme
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val pendingOccurrenceId = mutableStateOf<OccurrenceId?>(null)

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = appGraph.metroViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { mainViewModel.uiState.value == MainUiState.Loading }
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            handleEventIntent(intent)
        } else {
            // On recreation, the already handled intent comes back: only restore a destination not navigated to yet
            pendingOccurrenceId.value = savedInstanceState.getString(KEY_PENDING_OCCURRENCE_ID)?.toOccurrenceIdOrNull()
        }
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
                            pendingOccurrenceId = pendingOccurrenceId.value,
                            onClearPendingOccurrenceId = { pendingOccurrenceId.value = null },
                            onCalendarViewSelected = mainViewModel::saveCalendarView,
                        )
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pendingOccurrenceId.value?.let { outState.putString(KEY_PENDING_OCCURRENCE_ID, it.toJson()) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleEventIntent(intent)
    }

    private fun handleEventIntent(intent: Intent) {
        // Reopening the app from recents replays the intent its task was created with, so a notification already handled
        if ((intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) return
        val occurrenceIdJson = intent.getStringExtra(NotificationHelper.EXTRA_OCCURRENCE_ID_JSON) ?: return
        pendingOccurrenceId.value = occurrenceIdJson.toOccurrenceIdOrNull() ?: return
    }

    private fun OccurrenceId.toJson(): String = Json.encodeToString(OccurrenceId.serializer(), this)

    private fun String.toOccurrenceIdOrNull(): OccurrenceId? {
        return runCatching { Json.decodeFromString(OccurrenceId.serializer(), this) }.getOrNull()
    }

    companion object {
        private const val KEY_PENDING_OCCURRENCE_ID = "pendingOccurrenceId"
    }
}

@Composable
private fun MainContent(
    uiState: MainUiState.Loaded,
    visibleDayState: VisibleDayState,
    loadingEventsError: ReceiveChannel<SyncEventsManager.SyncError>,
    pendingOccurrenceId: OccurrenceId?,
    onClearPendingOccurrenceId: () -> Unit,
    onCalendarViewSelected: (NavDestination.CalendarView) -> Unit,
) {
    val isUserConnected = uiState.isUserConnected
    val startDestination = if (isUserConnected) uiState.lastCalendarView else NavDestination.Onboarding()
    val backStack = rememberNavDestinationBackStack(startDestination)

    LaunchedEffect(pendingOccurrenceId, isUserConnected) {
        val occurrenceId = pendingOccurrenceId ?: return@LaunchedEffect
        if (isUserConnected) {
            backStack.addOnce(NavDestination.EventDetail(occurrenceId))
            onClearPendingOccurrenceId()
        }
    }

    CompositionLocalProvider(
        LocalVisibleDayState provides visibleDayState,
        LocalSharedSnackbarHostState provides rememberCustomSnackbarHostState(),
        LocalToolbarScrollableState provides rememberToolbarScrollableState(),
        LocalDrawerState provides rememberDrawerState(initialValue = DrawerValue.Closed),
    ) {
        ObserveSyncError(loadingEventsError)
        NavigateToOnboardingIfLastUserIsDisconnected(backStack, isUserLoaded = isUserConnected)
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
