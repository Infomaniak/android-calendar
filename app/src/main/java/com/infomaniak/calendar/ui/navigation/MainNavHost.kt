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

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.infomaniak.calendar.utils.AccountUtils
import com.infomaniak.calendar.ui.screen.home.HomeScreen
import com.infomaniak.calendar.ui.screen.onboarding.CrossAppLoginViewModel
import com.infomaniak.calendar.ui.screen.onboarding.OnboardingScreen
import com.infomaniak.core.auth.models.UserLoginResult
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.utils.LoginUtils
import com.infomaniak.core.crossapplogin.back.BaseCrossAppLoginViewModel
import com.infomaniak.core.crossapplogin.back.CrossAppLoginFacade
import com.infomaniak.core.crossapplogin.back.ExternalAccount
import com.infomaniak.core.network.ApiEnvironment
import com.infomaniak.lib.login.InfomaniakLogin
import kotlinx.coroutines.launch

@Composable
fun MainNavHost(
    startDestination: NavDestination,
    crossAppLoginViewModel: CrossAppLoginViewModel,
    accountUtils: AccountUtils,
    infomaniakLogin: InfomaniakLogin,
) {
    val backStack = rememberNavBackStack(startDestination)
    NavDisplay(backStack = backStack, entryProvider = entryProvider {
        entry<NavDestination.Onboarding> { destination ->
            OnboardingEntry(
                requiredLogin = destination.requiredLogin,
                crossAppLoginViewModel = crossAppLoginViewModel,
                accountUtils = accountUtils,
                infomaniakLogin = infomaniakLogin,
                onNavigateToHome = {
                    backStack.clear()
                    backStack.add(NavDestination.Home)
                },
                onPopBack = { backStack.removeLastOrNull() },
            )
        }
        entry<NavDestination.Home> {
            HomeScreen()
        }
    })
}

@Composable
private fun OnboardingEntry(
    requiredLogin: Boolean,
    crossAppLoginViewModel: CrossAppLoginViewModel,
    accountUtils: AccountUtils,
    infomaniakLogin: InfomaniakLogin,
    onNavigateToHome: () -> Unit,
    onPopBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var areButtonsLoading by remember { mutableStateOf(false) }

    val accountsCheckingState by crossAppLoginViewModel.accountsCheckingState.collectAsStateWithLifecycle()
    val skippedIds by crossAppLoginViewModel.skippedAccountIds.collectAsStateWithLifecycle()

    suspend fun loginUsers(loginResult: CrossAppLoginFacade.LoginResult) {
        val results = LoginUtils.getLoginResultsAfterCrossApp(loginResult.tokens, context, accountUtils)
        val users = buildList {
            results.forEach { result ->
                when (result) {
                    is UserLoginResult.Success -> add(result.user)
                    is UserLoginResult.Failure -> snackbarHostState.showSnackbar(result.errorMessage)
                }
            }
        }

        if (users.isEmpty()) {
            areButtonsLoading = false
        } else {
            loginUsersIntoTheApp(users, requiredLogin, accountUtils, onNavigateToHome, onPopBack)
        }
    }

    suspend fun connectSelectedAccounts(accounts: List<ExternalAccount>, viewModel: BaseCrossAppLoginViewModel) {
        areButtonsLoading = true
        val loginResult = viewModel.attemptLogin(selectedAccounts = accounts)
        loginUsers(loginResult)
        loginResult.errorMessageIds.forEach { messageResId -> snackbarHostState.showSnackbar(context.getString(messageResId)) }
    }

    val loginFlowController = LoginUtils.rememberLoginFlowController(
        infomaniakLogin = infomaniakLogin,
        userExistenceChecker = accountUtils,
    ) { userLoginResult ->
        when (userLoginResult) {
            is UserLoginResult.Success -> scope.launch {
                loginUsersIntoTheApp(listOf(userLoginResult.user), requiredLogin, accountUtils, onNavigateToHome, onPopBack)
            }
            is UserLoginResult.Failure -> scope.launch { snackbarHostState.showSnackbar(userLoginResult.errorMessage) }
            null -> Unit // The user canceled the WebView.
        }

        if (userLoginResult !is UserLoginResult.Success) areButtonsLoading = false
    }

    OnboardingScreen(
        shouldDisplayRequiredLogin = requiredLogin,
        accountsCheckingState = { accountsCheckingState },
        skippedIds = { skippedIds },
        areLoginButtonsLoading = { areButtonsLoading },
        onLoginRequest = { accounts ->
            if (accounts.isEmpty()) {
                areButtonsLoading = true
                loginFlowController.login()
            } else {
                scope.launch { connectSelectedAccounts(accounts, crossAppLoginViewModel) }
            }
        },
        onCreateAccount = {
            areButtonsLoading = true
            loginFlowController.createAccount(CREATE_ACCOUNT_URL, CREATE_ACCOUNT_SUCCESS_HOST, CREATE_ACCOUNT_CANCEL_HOST)
        },
        onSaveSkippedAccounts = { crossAppLoginViewModel.skippedAccountIds.value = it },
        snackbarHostState = snackbarHostState,
    )
}

private suspend fun loginUsersIntoTheApp(
    users: List<User>,
    requiredLogin: Boolean,
    accountUtils: AccountUtils,
    onNavigateToHome: () -> Unit,
    onPopBack: () -> Unit,
) {
    users.forEach { user -> accountUtils.addUser(user) }
    if (requiredLogin) onPopBack() else onNavigateToHome()
}

private val host = ApiEnvironment.current.host
private val CREATE_ACCOUNT_URL = "https://welcome.$host/signup/infomaniak-calendar"
private val CREATE_ACCOUNT_SUCCESS_HOST = "shop.$host"
private const val CREATE_ACCOUNT_CANCEL_HOST = "" // No cancel host to detect.
