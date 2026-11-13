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
package com.infomaniak.calendar.ui.screen.eventDetail.locationSearch

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.foundation.component.SearchBar
import com.infomaniak.calendar.ui.component.topAppBar.SubmitTopAppBar
import com.infomaniak.calendar.ui.navigation.LocalSharedViewModelStoreOwner
import com.infomaniak.calendar.ui.screen.eventDetail.form.EventFormViewModel
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.calendar.components.resources.R as RComponents

@Composable
fun LocationSearchScreen(
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    eventFormViewModel: EventFormViewModel = viewModel(LocalSharedViewModelStoreOwner.current), // shared from parent
) {
    val formState = eventFormViewModel.formState ?: return

    LocationSearchScreen(
        initialLocation = formState.location.value,
        onSubmit = { location ->
            formState.location.value = location
            goBack()
        },
        goBack = goBack,
        modifier = modifier,
    )
}

@Composable
private fun LocationSearchScreen(
    initialLocation: String?,
    onSubmit: (String?) -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchState = rememberTextFieldState(initialLocation.orEmpty())

    Scaffold(
        topBar = {
            SubmitTopAppBar(
                goBack = goBack,
                onSubmit = { onSubmit(searchState.text.toString()) },
                isSubmitEnabled = { searchState.text.isNotBlank() },
                isRecurring = false,
                title = { Text(stringResource(RComponents.string.locationOrRoomLabel)) },
            )
        },
        modifier = modifier,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
    ) { contentPadding ->
        SearchBar(
            searchState = searchState,
            placeholder = stringResource(RComponents.string.locationSearchPlaceholder),
            modifier = Modifier
                .padding(contentPadding)
                .padding(top = EsdsTheme.spacing.xl)
                .padding(horizontal = EsdsTheme.spacing.xl),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        LocationSearchScreen(initialLocation = null, onSubmit = {}, goBack = {})
    }
}
