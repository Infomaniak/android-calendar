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
package com.infomaniak.calendar.ui.screen.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.infomaniak.calendar.MainActivity
import com.infomaniak.calendar.MainApplication
import com.infomaniak.calendar.ui.theme.CalendarTheme
import com.infomaniak.calendar.utils.AccountUtils
import com.infomaniak.core.auth.models.UserLoginResult
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.utils.LoginFlowController
import com.infomaniak.core.auth.utils.LoginUtils
import com.infomaniak.core.crossapplogin.back.BaseCrossAppLoginViewModel
import com.infomaniak.core.crossapplogin.back.CrossAppLoginFacade
import com.infomaniak.core.crossapplogin.back.ExternalAccount
import com.infomaniak.core.network.ApiEnvironment
import com.infomaniak.lib.login.InfomaniakLogin
import kotlinx.coroutines.launch

/**
 * Launches the onboarding flow and orchestrates both the WebView login and the cross-app login.
 *
 * Modeled after `com.infomaniak.swisstransfer.ui.OnboardingActivity`, with two intentional
 * differences:
 *  - Login is always required: there is no "connect as guest" path.
 *  - Two-factor authentication wiring is intentionally left out for now.
 *
 * Pass [EXTRA_REQUIRED_LOGIN_KEY]`=true` to reopen onboarding for a multi-account flow: only the
 * last (login) slide is shown, and finishing the login simply [finish]es the activity instead of
 * navigating to [MainActivity].
 */
class OnboardingActivity : ComponentActivity() {

    private val mainApplication by lazy { application as MainApplication }
    private val appGraph by lazy { mainApplication.appGraph }

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = appGraph.viewModelFactory

    private val crossAppLoginViewModel: CrossAppLoginViewModel by viewModels()

    private val accountUtils: AccountUtils by lazy { appGraph.accountUtils }
    private val infomaniakLogin: InfomaniakLogin by lazy { appGraph.infomaniakLogin }

    private var areButtonsLoading by mutableStateOf(false)

    private val shouldDisplayRequiredLogin by lazy { intent.getBooleanExtra(EXTRA_REQUIRED_LOGIN_KEY, false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupCrossAppLogin()

        setContent {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            CalendarTheme {
                val accountsCheckingState by crossAppLoginViewModel.accountsCheckingState.collectAsStateWithLifecycle()
                val skippedIds by crossAppLoginViewModel.skippedAccountIds.collectAsStateWithLifecycle()

                val loginFlowController = LoginUtils.rememberLoginFlowController(
                    infomaniakLogin = infomaniakLogin,
                    userExistenceChecker = accountUtils,
                ) { userLoginResult ->
                    when (userLoginResult) {
                        is UserLoginResult.Success -> loginUsersIntoTheApp(listOf(userLoginResult.user))
                        is UserLoginResult.Failure -> scope.launch { snackbarHostState.showSnackbar(userLoginResult.errorMessage) }
                        null -> Unit // The user canceled the WebView.
                    }

                    if (userLoginResult !is UserLoginResult.Success) stopLoadingLoginButtons()
                }

                Surface {
                    OnboardingScreen(
                        shouldDisplayRequiredLogin = shouldDisplayRequiredLogin,
                        accountsCheckingState = { accountsCheckingState },
                        skippedIds = { skippedIds },
                        areLoginButtonsLoading = { areButtonsLoading },
                        onLoginRequest = { accounts ->
                            if (accounts.isEmpty()) {
                                openLoginWebView(loginFlowController)
                            } else {
                                scope.launch { connectSelectedAccounts(accounts, crossAppLoginViewModel, snackbarHostState) }
                            }
                        },
                        onCreateAccount = { openAccountCreation(loginFlowController) },
                        onSaveSkippedAccounts = { crossAppLoginViewModel.skippedAccountIds.value = it },
                        snackbarHostState = snackbarHostState,
                    )
                }
            }
        }
    }

    private fun setupCrossAppLogin() {
        lifecycleScope.launch {
            crossAppLoginViewModel.activateUpdates(this@OnboardingActivity)
        }
    }

    private fun loginUsersIntoTheApp(users: List<User>) {
        lifecycleScope.launch {
            users.forEach { user -> accountUtils.addUser(user) }
            if (shouldDisplayRequiredLogin) finish() else completeOnboarding()
        }
    }

    private fun openLoginWebView(loginFlowController: LoginFlowController) {
        startLoadingLoginButtons()
        loginFlowController.login()
    }

    private fun openAccountCreation(loginFlowController: LoginFlowController) {
        startLoadingLoginButtons()
        loginFlowController.createAccount(CREATE_ACCOUNT_URL, CREATE_ACCOUNT_SUCCESS_HOST, CREATE_ACCOUNT_CANCEL_HOST)
    }

    private suspend fun connectSelectedAccounts(
        accounts: List<ExternalAccount>,
        viewModel: BaseCrossAppLoginViewModel,
        snackbarHostState: SnackbarHostState,
    ) {
        startLoadingLoginButtons()
        val loginResult = viewModel.attemptLogin(selectedAccounts = accounts)
        loginUsers(loginResult, snackbarHostState)
        loginResult.errorMessageIds.forEach { messageResId -> snackbarHostState.showSnackbar(getString(messageResId)) }
    }

    private suspend fun loginUsers(loginResult: CrossAppLoginFacade.LoginResult, snackbarHostState: SnackbarHostState) {
        val results = LoginUtils.getLoginResultsAfterCrossApp(loginResult.tokens, this, accountUtils)
        val users = buildList {
            results.forEach { result ->
                when (result) {
                    is UserLoginResult.Success -> add(result.user)
                    is UserLoginResult.Failure -> snackbarHostState.showSnackbar(result.errorMessage)
                }
            }
        }

        if (users.isEmpty()) {
            stopLoadingLoginButtons()
        } else {
            loginUsersIntoTheApp(users)
        }
    }

    private fun startLoadingLoginButtons() {
        areButtonsLoading = true
    }

    private fun stopLoadingLoginButtons() {
        areButtonsLoading = false
    }

    private fun completeOnboarding() {
        // TODO: Persist a "onboarding done" flag when the account / preferences layer exists.
        Intent(this, MainActivity::class.java).also(::startActivity)
        finish()
    }

    companion object {
        const val EXTRA_REQUIRED_LOGIN_KEY = "EXTRA_REQUIRED_LOGIN_KEY"

        private val host = ApiEnvironment.current.host
        private val CREATE_ACCOUNT_URL = "https://welcome.$host/signup/infomaniak-calendar"
        private val CREATE_ACCOUNT_SUCCESS_HOST = "shop.$host"
        private const val CREATE_ACCOUNT_CANCEL_HOST = "" // No cancel host to detect.
    }
}
