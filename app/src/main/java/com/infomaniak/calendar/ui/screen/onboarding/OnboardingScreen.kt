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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.R
import com.infomaniak.core.crossapplogin.back.CrossAppLoginFacade.AccountsCheckingState
import com.infomaniak.core.crossapplogin.back.CrossAppLoginFacade.AccountsCheckingStatus
import com.infomaniak.core.crossapplogin.back.ExternalAccount
import com.infomaniak.core.crossapplogin.front.components.CrossLoginBottomContent
import com.infomaniak.core.crossapplogin.front.components.NoCrossAppLoginAccountsContent
import com.infomaniak.core.onboarding.OnboardingPage
import com.infomaniak.core.onboarding.OnboardingScaffold
import com.infomaniak.core.onboarding.components.OnboardingComponents

/**
 * Calendar onboarding screen.
 *
 * Modeled after `com.infomaniak.swisstransfer.ui.screen.onboarding.OnboardingScreen`, with two
 * deliberate differences:
 *  - **Login is always required.** The bottom content always uses
 *    [NoCrossAppLoginAccountsContent.accountRequired]; there is no "continue as guest" path.
 *  - **No connectAsGuest callback** is exposed.
 *
 * Multi-account is still supported via [shouldDisplayRequiredLogin]: when the activity is reopened
 * from a logged-in user wanting to add another account, only the last (login) slide is shown.
 *
 * @param shouldDisplayRequiredLogin When `true`, only the last login slide is shown and the
 * carousel is skipped. Use this when reopening onboarding from a multi-account flow to add another
 * account.
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
fun OnboardingScreen(
    shouldDisplayRequiredLogin: Boolean,
    accountsCheckingState: () -> AccountsCheckingState,
    skippedIds: () -> Set<Long>,
    areLoginButtonsLoading: () -> Boolean,
    onLoginRequest: (accounts: List<ExternalAccount>) -> Unit,
    onCreateAccount: () -> Unit,
    onSaveSkippedAccounts: (Set<Long>) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    // When relaunching onboarding from a multi-account flow we skip straight to the login slide
    // (the last entry, by convention).
    val pages: List<Page> = if (shouldDisplayRequiredLogin) listOf(Page.entries.last()) else Page.entries

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

/**
 * Dummy slides used until the real onboarding content is supplied. Order matters: the last entry
 * is always the "login" slide and is the only one shown when [OnboardingScreen.shouldDisplayRequiredLogin]
 * is `true`.
 */
private enum class Page(
    @DrawableRes val illustrationRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
) {
    PageOne(
        illustrationRes = R.drawable.illu_onboarding_placeholder,
        titleRes = R.string.onboardingPageOneTitle,
        descriptionRes = R.string.onboardingPageOneDescription,
    ),
    PageTwo(
        illustrationRes = R.drawable.illu_onboarding_placeholder,
        titleRes = R.string.onboardingPageTwoTitle,
        descriptionRes = R.string.onboardingPageTwoDescription,
    ),
    PageThree(
        illustrationRes = R.drawable.illu_onboarding_placeholder,
        titleRes = R.string.onboardingPageThreeTitle,
        descriptionRes = R.string.onboardingPageThreeDescription,
    ),
    PageFour(
        illustrationRes = R.drawable.illu_onboarding_placeholder,
        titleRes = R.string.onboardingPageFourTitle,
        descriptionRes = R.string.onboardingPageFourDescription,
    );

    fun toOnboardingPage(): OnboardingPage = OnboardingPage(
        background = {
            // Solid background; replace with a themed gradient/illustration when the real assets land.
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(descriptionRes),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        },
    )
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    MaterialTheme {
        Surface {
            OnboardingScreen(
                shouldDisplayRequiredLogin = false,
                accountsCheckingState = { AccountsCheckingState(status = AccountsCheckingStatus.UpToDate) },
                skippedIds = { emptySet() },
                areLoginButtonsLoading = { false },
                onLoginRequest = {},
                onCreateAccount = {},
                onSaveSkippedAccounts = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
        }
    }
}
