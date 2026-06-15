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

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.R
import com.infomaniak.calendar.di.ComposeAppGraph
import com.infomaniak.calendar.ui.navigation.state.LocalSharedSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.SharedSnackbarHostState
import com.infomaniak.calendar.utils.AccountUtils
import com.infomaniak.core.auth.models.UserLoginResult
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.utils.LoginFlowController
import com.infomaniak.core.auth.utils.LoginUtils
import com.infomaniak.core.crossapplogin.back.BaseCrossAppLoginViewModel
import com.infomaniak.core.crossapplogin.back.CrossAppLoginFacade
import com.infomaniak.core.crossapplogin.back.CrossAppLoginFacade.AccountsCheckingState
import com.infomaniak.core.crossapplogin.back.CrossAppLoginFacade.AccountsCheckingStatus
import com.infomaniak.core.crossapplogin.back.ExternalAccount
import com.infomaniak.core.crossapplogin.front.components.CrossLoginBottomContent
import com.infomaniak.core.crossapplogin.front.components.NoCrossAppLoginAccountsContent
import com.infomaniak.core.login.InfomaniakLogin
import com.infomaniak.core.network.ApiEnvironment
import com.infomaniak.core.onboarding.OnboardingPage
import com.infomaniak.core.onboarding.OnboardingScaffold
import com.infomaniak.core.onboarding.components.OnboardingComponents
import com.infomaniak.core.onboarding.components.OnboardingComponents.DefaultTitleAndDescription
import kotlinx.coroutines.launch

private val host = ApiEnvironment.current.host
private val CREATE_ACCOUNT_URL = "https://welcome.$host/signup/swisstransfer" // TODO[login]: Use the correct account creation url
private val CREATE_ACCOUNT_SUCCESS_HOST = "shop.$host"
private const val CREATE_ACCOUNT_CANCEL_HOST = "" // No cancel host to detect.

@Composable
fun OnboardingScreen(
    onlyLogin: Boolean,
    goToCalendarView: () -> Unit,
    onPopBack: () -> Unit,
    crossAppLoginViewModel: CrossAppLoginViewModel = viewModel(),
    accountUtils: AccountUtils = ComposeAppGraph.accountUtils,
    infomaniakLogin: InfomaniakLogin = ComposeAppGraph.infomaniakLogin,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hostActivity = LocalActivity.current
    val snackbarHostState = LocalSharedSnackbarHostState.current
    var areButtonsLoading by remember { mutableStateOf(false) }

    val accountsCheckingState by crossAppLoginViewModel.accountsCheckingState.collectAsStateWithLifecycle()
    val skippedIds by crossAppLoginViewModel.skippedAccountIds.collectAsStateWithLifecycle()

    val loginDependencies = OnboardingLoginDependencies(
        context = context,
        accountUtils = accountUtils,
        snackbarHostState = snackbarHostState,
        onlyLoginScreen = onlyLogin,
        setButtonsLoading = { areButtonsLoading = it },
        onNavigateToHome = goToCalendarView,
        onPopBack = onPopBack,
    )
    val loginFlowController = rememberOnboardingLoginFlowController(
        infomaniakLogin = infomaniakLogin,
        dependencies = loginDependencies,
    )

    LaunchedEffect(Unit) {
        if (hostActivity !is ComponentActivity) return@LaunchedEffect
        crossAppLoginViewModel.activateUpdates(hostActivity)
    }

    OnboardingScreen(
        onlyLogin = onlyLogin,
        accountsCheckingState = { accountsCheckingState },
        skippedIds = { skippedIds },
        areLoginButtonsLoading = { areButtonsLoading },
        onLoginRequest = { accounts ->
            if (accounts.isEmpty()) {
                loginDependencies.setButtonsLoading(true)
                loginFlowController.login()
            } else {
                scope.launch {
                    connectSelectedAccounts(
                        accounts = accounts,
                        viewModel = crossAppLoginViewModel,
                        dependencies = loginDependencies,
                    )
                }
            }
        },
        onCreateAccount = {
            loginDependencies.setButtonsLoading(true)
            loginFlowController.createAccount(CREATE_ACCOUNT_URL, CREATE_ACCOUNT_SUCCESS_HOST, CREATE_ACCOUNT_CANCEL_HOST)
        },
        onSaveSkippedAccounts = { crossAppLoginViewModel.skippedAccountIds.value = it },
    )
}

/**
 * @param onlyLogin When `true`, only the last login slide is shown and the carousel is skipped.
 * Use this when reopening onboarding from a multi-account flow to add another account.
 * @param accountsCheckingState The cross-app login facade state (loading / accounts / error).
 * @param skippedIds Cross-app accounts the user has chosen to skip.
 * @param areLoginButtonsLoading Whether the login / sign-up buttons should currently show a loader
 * (e.g. while we resolve the WebView / cross-app login result).
 * @param onLoginRequest Called when the user asks to log in. The list contains the cross-app
 * accounts the user selected, or is empty when the user wants to log in a brand new account via
 * the WebView.
 * @param onCreateAccount Called when the user wants to create a brand new Infomaniak account via
 * the WebView.
 * @param onSaveSkippedAccounts Persists the latest selection of skipped cross-app accounts.
 */
@Composable
private fun OnboardingScreen(
    onlyLogin: Boolean,
    accountsCheckingState: () -> AccountsCheckingState,
    skippedIds: () -> Set<Long>,
    areLoginButtonsLoading: () -> Boolean,
    onLoginRequest: (accounts: List<ExternalAccount>) -> Unit,
    onCreateAccount: () -> Unit,
    onSaveSkippedAccounts: (Set<Long>) -> Unit,
) {
    val pages: List<Page> = if (onlyLogin) listOf(Page.entries.last()) else Page.entries

    val pagerState = rememberPagerState(pageCount = { pages.size })

    OnboardingScaffold(
        pagerState = pagerState,
        onboardingPages = pages.mapIndexed { _, page -> page.toOnboardingPage() },
        bottomContent = { paddingValues ->
            OnboardingComponents.CrossLoginBottomContent(
                modifier = Modifier
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
                pagerState = pagerState,
                isLoginButtonLoading = areLoginButtonsLoading,
                accountsCheckingState = accountsCheckingState,
                skippedIds = skippedIds,
                onContinueWithSelectedAccounts = { selectedAccounts -> onLoginRequest(selectedAccounts) },
                onUseAnotherAccountClicked = { onLoginRequest(emptyList()) },
                onSaveSkippedAccounts = onSaveSkippedAccounts,
                // Login is always required, regardless of whether cross-app accounts exist.
                noCrossAppLoginAccountsContent = NoCrossAppLoginAccountsContent.accountRequired(
                    onLogin = { onLoginRequest(emptyList()) },
                    onCreateAccount = onCreateAccount,
                    isLoginButtonLoading = areLoginButtonsLoading,
                    isSignUpButtonLoading = areLoginButtonsLoading,
                ),
            )
        },
    )
}

/**
 * Dummy slides for now
 */
private enum class Page(
    @DrawableRes val illustrationRes: Int,
    val title: String,
    val description: String,
) {
    PageOne(
        illustrationRes = R.drawable.illu_onboarding_placeholder,
        title = "Dummy onboarding title #1",
        description = "Dummy onboarding description for the first slide. Replace me with the real copy.",
    ),
    PageTwo(
        illustrationRes = R.drawable.illu_onboarding_placeholder,
        title = "Dummy onboarding title #2",
        description = "Dummy onboarding description for the second slide. Replace me with the real copy.",
    ),
    PageThree(
        illustrationRes = R.drawable.illu_onboarding_placeholder,
        title = "Dummy onboarding title #3",
        description = "Dummy onboarding description for the third slide. Replace me with the real copy.",
    ),
    PageFour(
        illustrationRes = R.drawable.illu_onboarding_placeholder,
        title = "Sign in to your Infomaniak account",
        description = "Sign in to start using the Infomaniak Calendar.",
    );

    fun toOnboardingPage(): OnboardingPage = OnboardingPage(
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            )
        },
        illustration = {
            Image(
                painter = painterResource(illustrationRes),
                contentDescription = null,
                modifier = Modifier.size(240.dp),
            )
        },
        text = {
            DefaultTitleAndDescription(
                title = title,
                description = description,
                titleStyle = MaterialTheme.typography.headlineMedium,
                descriptionStyle = MaterialTheme.typography.bodyLarge,
            )
        },
    )
}

@Composable
private fun rememberOnboardingLoginFlowController(
    infomaniakLogin: InfomaniakLogin,
    dependencies: OnboardingLoginDependencies,
): LoginFlowController {
    val scope = rememberCoroutineScope()

    return LoginUtils.rememberLoginFlowController(
        infomaniakLogin = infomaniakLogin,
        userExistenceChecker = dependencies.accountUtils,
    ) { userLoginResult ->
        when (userLoginResult) {
            is UserLoginResult.Success -> scope.launch {
                loginUsersIntoTheApp(
                    users = listOf(userLoginResult.user),
                    onlyLoginScreen = dependencies.onlyLoginScreen,
                    accountUtils = dependencies.accountUtils,
                    onNavigateToHome = dependencies.onNavigateToHome,
                    onPopBack = dependencies.onPopBack,
                )
            }
            is UserLoginResult.Failure -> {
                dependencies.snackbarHostState?.showSnackbar(userLoginResult.errorMessage)
            }
            null -> Unit // The user canceled the WebView.
        }

        if (userLoginResult !is UserLoginResult.Success) dependencies.setButtonsLoading(false)
    }
}

private suspend fun connectSelectedAccounts(
    accounts: List<ExternalAccount>,
    viewModel: BaseCrossAppLoginViewModel,
    dependencies: OnboardingLoginDependencies,
) {
    dependencies.setButtonsLoading(true)
    val loginResult = viewModel.attemptLogin(selectedAccounts = accounts)
    loginUsers(loginResult = loginResult, dependencies = dependencies)
    loginResult.errorMessageIds.forEach { messageResId ->
        dependencies.snackbarHostState?.showSnackbar(dependencies.context.getString(messageResId))
    }
}

private suspend fun loginUsers(loginResult: CrossAppLoginFacade.LoginResult, dependencies: OnboardingLoginDependencies) {
    val results = LoginUtils.getLoginResultsAfterCrossApp(
        apiTokens = loginResult.tokens,
        context = dependencies.context,
        userExistenceChecker = dependencies.accountUtils,
    )
    val users = buildList {
        results.forEach { result ->
            when (result) {
                is UserLoginResult.Success -> add(result.user)
                is UserLoginResult.Failure -> dependencies.snackbarHostState?.showSnackbar(result.errorMessage)
            }
        }
    }

    if (users.isEmpty()) {
        dependencies.setButtonsLoading(false)
    } else {
        loginUsersIntoTheApp(
            users = users,
            onlyLoginScreen = dependencies.onlyLoginScreen,
            accountUtils = dependencies.accountUtils,
            onNavigateToHome = dependencies.onNavigateToHome,
            onPopBack = dependencies.onPopBack,
        )
    }
}

private suspend fun loginUsersIntoTheApp(
    users: List<User>,
    onlyLoginScreen: Boolean,
    accountUtils: AccountUtils,
    onNavigateToHome: () -> Unit,
    onPopBack: () -> Unit,
) {
    users.forEach { user -> accountUtils.addUser(user) }
    if (onlyLoginScreen) onPopBack() else onNavigateToHome()
}

private data class OnboardingLoginDependencies(
    val context: Context,
    val accountUtils: AccountUtils,
    val snackbarHostState: SharedSnackbarHostState?,
    val onlyLoginScreen: Boolean,
    val setButtonsLoading: (Boolean) -> Unit,
    val onNavigateToHome: () -> Unit,
    val onPopBack: () -> Unit,
)

@Preview
@Composable
private fun OnboardingScreenPreview() {
    MaterialTheme {
        Surface {
            OnboardingScreen(
                onlyLogin = false,
                accountsCheckingState = { AccountsCheckingState(status = AccountsCheckingStatus.UpToDate) },
                skippedIds = { emptySet() },
                areLoginButtonsLoading = { false },
                onLoginRequest = {},
                onCreateAccount = {},
                onSaveSkippedAccounts = {},
            )
        }
    }
}
