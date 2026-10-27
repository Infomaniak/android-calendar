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
package com.infomaniak.calendar.ui.screen.onboarding.permissions

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.onboarding.OnboardingPage
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.coroutines.launch

@Composable
fun PermissionsOnboardingScreen(goToCalendarView: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pages = rememberSaveable { PermissionsOnboardingPage.pagesToShow(context) }
    val pagerState = rememberPagerState(pageCount = { pages.size })

    fun goToNextPage() {
        if (pagerState.currentPage == pages.lastIndex) {
            goToCalendarView()
        } else {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { goToNextPage() }

    BackHandler(onBack = goToCalendarView)

    PermissionsOnboardingScreen(
        pages = pages,
        pagerState = pagerState,
        onNextClicked = {
            val permission = pages[pagerState.currentPage].permission
            if (permission == null) goToNextPage() else permissionLauncher.launch(permission)
        },
    )
}

@Composable
private fun PermissionsOnboardingScreen(
    pages: List<PermissionsOnboardingPage>,
    pagerState: PagerState,
    onNextClicked: () -> Unit,
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {
            HorizontalPager(state = pagerState, userScrollEnabled = false, modifier = Modifier.weight(1f)) { page ->
                OnboardingPageContent(pages[page].toOnboardingPage())
            }

            NextStepButton(
                page = pages[pagerState.currentPage],
                onClick = onNextClicked,
                modifier = Modifier.padding(vertical = Margin.Large),
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        page.background()
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            page.illustration()
            page.text()
        }
    }
}

@Preview
@Composable
private fun PermissionsOnboardingScreenPreview() {
    CalendarThemeForPreview {
        val pages = PermissionsOnboardingPage.entries
        PermissionsOnboardingScreen(pages = pages, pagerState = rememberPagerState { pages.size }, onNextClicked = {})
    }
}
