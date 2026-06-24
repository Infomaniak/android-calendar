package com.infomaniak.calendar

import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.infomaniak.calendar.ui.LocalUser
import com.infomaniak.calendar.ui.navigation.MainNavHost
import com.infomaniak.calendar.ui.navigation.NavDestination
import com.infomaniak.calendar.ui.theme.CalendarTheme
import com.infomaniak.calendar.utils.UserLoadState
import com.infomaniak.calendar.utils.rememberUserLoadState
import com.infomaniak.core.auth.models.user.User
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val mainApplication by lazy { application as MainApplication }
    private val appGraph by lazy { mainApplication.appGraph }

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = appGraph.viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (SDK_INT >= 29) window.isNavigationBarContrastEnforced = false

        lifecycleScope.launch {
            appGraph.securedDavCredentialsRepository.loadAndInitAllCredentials()
        }

        setContent {
            CalendarTheme {
                Surface {
                    when (val userLoadState = appGraph.accountUtils.rememberUserLoadState().value) {
                        UserLoadState.Awaiting -> Unit // Blank surface while waiting for first result
                        is UserLoadState.Loaded -> MainContent(userLoadState)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainContent(userLoadState: UserLoadState.Loaded) {
    val startDestination = if (userLoadState.user == null) NavDestination.Onboarding() else NavDestination.CalendarView.Planning
    val backStack = rememberNavBackStack(startDestination)

    CompositionLocalProvider(LocalUser provides userLoadState.user) {
        NavigateToOnboardingIfLastUserIsDisconnected(backStack, userLoadState.user)
        MainNavHost(backStack)
    }
}

@Composable
private fun NavigateToOnboardingIfLastUserIsDisconnected(backStack: NavBackStack<NavKey>, user: User?) {
    val isUserLoaded = user != null

    LaunchedEffect(isUserLoaded) {
        if (isUserLoaded.not()) {
            backStack.clear()
            backStack.add(NavDestination.Onboarding())
        }
    }
}
