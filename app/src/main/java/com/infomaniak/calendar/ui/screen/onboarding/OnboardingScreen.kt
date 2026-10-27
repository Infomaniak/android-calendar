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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.di.ComposeAppGraph
import com.infomaniak.calendar.ui.navigation.state.LocalSharedSnackbarHostState
import com.infomaniak.calendar.ui.navigation.state.SharedSnackbarHostState
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.calendar.utils.account.CalendarUser
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
import com.infomaniak.core.crossapplogin.front.data.CrossLoginDefaults
import com.infomaniak.core.login.InfomaniakLogin
import com.infomaniak.core.network.ApiEnvironment
import com.infomaniak.core.onboarding.OnboardingScaffold
import com.infomaniak.core.onboarding.components.OnboardingComponents
import com.infomaniak.multiplatform_calendar.core.domain.model.account.DavCredentials
import com.infomaniak.multiplatform_calendar.core.managers.AccountManager
import com.infomaniak.multiplatform_core.account.domain.model.AccessToken
import kotlinx.coroutines.launch
import com.infomaniak.core.common.R as RCore

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
    accountManager: AccountManager = ComposeAppGraph.accountManager,
    infomaniakLogin: InfomaniakLogin = ComposeAppGraph.infomaniakLogin,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hostActivity = LocalActivity.current
    val snackbarHostState = LocalSharedSnackbarHostState.current
    var areButtonsLoading by remember { mutableStateOf(false) }

    var step by rememberSaveable { mutableStateOf(OnboardingStep.Login) }

    val accountsCheckingState by crossAppLoginViewModel.accountsCheckingState.collectAsStateWithLifecycle()
    val skippedIds by crossAppLoginViewModel.skippedAccountIds.collectAsStateWithLifecycle()

    fun goToNextStep() {
        val nextStep = step.next(context)
        if (nextStep == null) goToCalendarView() else step = nextStep
    }

    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { goToNextStep() }

    val loginDependencies = OnboardingLoginDependencies(
        context = context,
        accountUtils = accountUtils,
        accountManager = accountManager,
        snackbarHostState = snackbarHostState,
        onlyLoginScreen = onlyLogin,
        setButtonsLoading = { areButtonsLoading = it },
        onLoggedIn = { goToNextStep() },
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
        step = step,
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
        onNextClicked = {
            val permission = step.permission
            if (permission == null) goToNextStep() else permissionLauncher.launch(permission)
        },
    )
}

/**
 * @param step The step shown: the login, then the permissions it still lacks, then the end of the onboarding.
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
 * @param onNextClicked Called by the button of every step after the login.
 */
@Composable
private fun OnboardingScreen(
    step: OnboardingStep,
    accountsCheckingState: () -> AccountsCheckingState,
    skippedIds: () -> Set<Long>,
    areLoginButtonsLoading: () -> Boolean,
    onLoginRequest: (accounts: List<ExternalAccount>) -> Unit,
    onCreateAccount: () -> Unit,
    onSaveSkippedAccounts: (Set<Long>) -> Unit,
    onNextClicked: () -> Unit,
) {
    AnimatedContent(
        targetState = step,
        transitionSpec = { slideIntoContainer(SlideDirection.Start) togetherWith slideOutOfContainer(SlideDirection.Start) },
    ) { targetStep ->
        // A single page per step: the pager can't be swiped and shows no indicator.
        val pagerState = rememberPagerState(pageCount = { 1 })

        OnboardingScaffold(
            pagerState = pagerState,
            onboardingPages = listOf(targetStep.toOnboardingPage()),
            bottomContent = { paddingValues ->
                val modifier = Modifier
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)

                if (targetStep == OnboardingStep.Login) {
                    OnboardingComponents.CrossLoginBottomContent(
                        modifier = modifier,
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
                        customization = CrossLoginDefaults.customize(buttonStyle = OnboardingButtonDefaults.crossLoginStyle),
                    )
                } else {
                    NextStepButton(step = targetStep, onClick = onNextClicked, modifier = modifier)
                }
            },
        )
    }
}

private suspend fun fetchDavCredentials(
    user: User,
    accountManager: AccountManager,
): DavCredentials? = runCatching {
    accountManager.retrieveDavCredential(authToken = AccessToken(user.apiToken.accessToken), login = user.login)
}.getOrNull()

@Composable
private fun rememberOnboardingLoginFlowController(
    infomaniakLogin: InfomaniakLogin,
    dependencies: OnboardingLoginDependencies,
): LoginFlowController = LoginUtils.rememberLoginFlowController(infomaniakLogin, dependencies.accountUtils) { userLoginResult ->
    val user: User = when (userLoginResult) {
        is UserLoginResult.Success -> userLoginResult.user
        is UserLoginResult.Failure -> {
            dependencies.snackbarHostState?.showSnackbar(userLoginResult.errorMessage)
            null
        }
        null -> null
    } ?: run {
        dependencies.setButtonsLoading(false)
        return@rememberLoginFlowController
    }

    val davCredentials = fetchDavCredentials(user, dependencies.accountManager) ?: run {
        dependencies.snackbarHostState?.showSnackbar(dependencies.context.getString(RCore.string.anErrorHasOccurred))
        dependencies.setButtonsLoading(false)
        return@rememberLoginFlowController
    }

    loginUsersIntoTheApp(
        calendarUsers = listOf(CalendarUser(user, davCredentials)),
        onlyLoginScreen = dependencies.onlyLoginScreen,
        accountUtils = dependencies.accountUtils,
        onLoggedIn = dependencies.onLoggedIn,
        onPopBack = dependencies.onPopBack,
    )
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

    val users: List<User> = buildList {
        results.forEach { result ->
            when (result) {
                is UserLoginResult.Success -> add(result.user)
                is UserLoginResult.Failure -> dependencies.snackbarHostState?.showSnackbar(result.errorMessage)
            }
        }
    }

    val calendarUsers: List<CalendarUser> = buildList {
        users.forEach { user ->
            val davCredentials = fetchDavCredentials(user, dependencies.accountManager) ?: run {
                dependencies.snackbarHostState?.showSnackbar(dependencies.context.getString(RCore.string.anErrorHasOccurred))
                return@forEach
            }

            add(CalendarUser(user, davCredentials))
        }
    }

    if (calendarUsers.isEmpty()) {
        dependencies.setButtonsLoading(false)
    } else {
        loginUsersIntoTheApp(
            calendarUsers = calendarUsers,
            onlyLoginScreen = dependencies.onlyLoginScreen,
            accountUtils = dependencies.accountUtils,
            onLoggedIn = dependencies.onLoggedIn,
            onPopBack = dependencies.onPopBack,
        )
    }
}

private suspend fun loginUsersIntoTheApp(
    calendarUsers: List<CalendarUser>,
    onlyLoginScreen: Boolean,
    accountUtils: AccountUtils,
    onLoggedIn: () -> Unit,
    onPopBack: () -> Unit,
) {
    calendarUsers.forEach { calendarUser ->
        accountUtils.addUser(calendarUser = calendarUser)
    }
    if (onlyLoginScreen) onPopBack() else onLoggedIn()
}

private data class OnboardingLoginDependencies(
    val context: Context,
    val accountUtils: AccountUtils,
    val accountManager: AccountManager,
    val snackbarHostState: SharedSnackbarHostState?,
    val onlyLoginScreen: Boolean,
    val setButtonsLoading: (Boolean) -> Unit,
    val onLoggedIn: () -> Unit,
    val onPopBack: () -> Unit,
)

@Preview
@Composable
private fun OnboardingScreenPreview() {
    MaterialTheme {
        Surface {
            OnboardingScreen(
                step = OnboardingStep.Login,
                accountsCheckingState = { AccountsCheckingState(status = AccountsCheckingStatus.UpToDate) },
                skippedIds = { emptySet() },
                areLoginButtonsLoading = { false },
                onLoginRequest = {},
                onCreateAccount = {},
                onSaveSkippedAccounts = {},
                onNextClicked = {},
            )
        }
    }
}
