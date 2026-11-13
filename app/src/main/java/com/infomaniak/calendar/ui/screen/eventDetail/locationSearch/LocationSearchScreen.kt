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
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.eventdetail.models.EventDetailUi
import com.infomaniak.calendar.components.locationsearch.LocationSearch
import com.infomaniak.calendar.components.locationsearch.models.LocationSearchResultUi
import com.infomaniak.calendar.components.locationsearch.models.RoomAvailability
import com.infomaniak.calendar.components.locationsearch.models.SearchPlaceUi
import com.infomaniak.calendar.components.locationsearch.models.SearchRoomUi
import com.infomaniak.calendar.ui.component.topAppBar.SubmitTopAppBar
import com.infomaniak.calendar.ui.navigation.LocalSharedViewModelStoreOwner
import com.infomaniak.calendar.ui.screen.eventDetail.form.EventFormViewModel
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import com.infomaniak.calendar.components.resources.R as RComponents

@Composable
fun LocationSearchScreen(
    editedOccurrenceId: OccurrenceId?,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    locationSearchViewModel: LocationSearchViewModel = viewModel(LocalSharedViewModelStoreOwner.current), // shared from parent
    eventFormViewModel: EventFormViewModel = viewModel(LocalSharedViewModelStoreOwner.current), // shared from parent
) {
    val formState = eventFormViewModel.formState ?: return
    val searchState = rememberTextFieldState((formState.location as? EventDetailUi.LocationUi.Place)?.name.orEmpty())
    val results by locationSearchViewModel.results.collectAsStateWithLifecycle()

    LaunchedEffect(formState) {
        locationSearchViewModel.followForm(formState, editedOccurrenceId)
    }

    LaunchedEffect(searchState) {
        snapshotFlow { searchState.text.toString() }.collect(locationSearchViewModel::search)
    }

    LocationSearchScreen(
        searchState = searchState,
        results = { results },
        onPlaceClick = { place ->
            formState.location = EventDetailUi.LocationUi.Place(name = place.name, address = place.address)
            goBack()
        },
        onRoomClick = { room ->
            formState.location = locationSearchViewModel.formRoomOf(room)
            goBack()
        },
        goBack = goBack,
        modifier = modifier,
    )
}

@Composable
private fun LocationSearchScreen(
    searchState: TextFieldState,
    results: () -> List<LocationSearchResultUi>,
    onPlaceClick: (SearchPlaceUi) -> Unit,
    onRoomClick: (SearchRoomUi) -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            SubmitTopAppBar(
                goBack = goBack,
                onSubmit = { onPlaceClick(SearchPlaceUi(name = searchState.text.toString(), address = null)) },
                isSubmitEnabled = { searchState.text.isNotBlank() },
                isRecurring = false,
                title = { Text(stringResource(RComponents.string.locationOrRoomLabel)) },
            )
        },
        modifier = modifier,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
    ) { contentPadding ->
        LocationSearch(
            searchState = searchState,
            results = results,
            onPlaceClick = onPlaceClick,
            onRoomClick = onRoomClick,
            contentPadding = contentPadding,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        LocationSearchScreen(
            searchState = rememberTextFieldState("Hall"),
            results = {
                listOf(
                    SearchPlaceUi(name = "Community Hall", address = "Rue de Lausanne 1, 1201 Geneva, Switzerland"),
                    SearchRoomUi(
                        id = "1",
                        name = "Jules Verne room",
                        seats = 100,
                        floor = 3,
                        availability = RoomAvailability.Available,
                    ),
                )
            },
            onPlaceClick = {},
            onRoomClick = {},
            goBack = {},
        )
    }
}
