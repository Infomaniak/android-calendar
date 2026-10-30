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
package com.infomaniak.calendar.ui.screen.eventDetail.timeZoneSearch

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.timezonesearch.TimeZoneSearch
import com.infomaniak.calendar.ui.component.topAppBar.TopAppBarButtons
import com.infomaniak.calendar.ui.navigation.LocalSharedViewModelStoreOwner
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import kotlinx.datetime.TimeZone

@Composable
fun TimeZoneSearchScreen(
    isStart: Boolean,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    timeZoneSearchResultViewModel: TimeZoneSearchResultViewModel = viewModel(LocalSharedViewModelStoreOwner.current), // shared from parent
) {
    TimeZoneSearchScreen(
        onTimeZoneSelected = { timeZone ->
            timeZoneSearchResultViewModel.pickTimeZone(isStart, timeZone)
            goBack()
        },
        goBack = goBack,
        modifier = modifier,
    )
}

@Composable
private fun TimeZoneSearchScreen(
    onTimeZoneSelected: (TimeZone) -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { TopAppBarButtons.BackButton(onClick = goBack) },
                title = {},
            )
        },
        modifier = modifier,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
    ) { contentPadding ->
        TimeZoneSearch(
            onTimeZoneClick = onTimeZoneSelected,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = contentPadding,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        TimeZoneSearchScreen(onTimeZoneSelected = {}, goBack = {})
    }
}
