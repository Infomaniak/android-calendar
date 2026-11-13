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
package com.infomaniak.calendar.components.locationsearch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.component.SearchBar
import com.infomaniak.calendar.components.foundation.component.SearchLayout
import com.infomaniak.calendar.components.foundation.models.LocationUi
import com.infomaniak.calendar.components.foundation.models.RoomAvailability
import com.infomaniak.calendar.components.foundation.utils.onlyHorizontalAndTop
import com.infomaniak.calendar.components.locationsearch.component.PlaceItem
import com.infomaniak.calendar.components.locationsearch.component.RoomItem
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme

/** Lists [results] as given, under the search bar editing [searchState]. */
@Composable
fun LocationSearch(
    searchState: TextFieldState,
    results: () -> List<LocationUi>,
    onLocationClick: (LocationUi) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    SearchLayout(modifier = modifier, contentPadding = contentPadding.onlyHorizontalAndTop()) {
        SearchBar(searchState, placeholder = stringResource(R.string.locationSearchPlaceholder))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + EsdsTheme.spacing.sm),
        ) {
            val results = results()

            itemsIndexed(results, key = { index, result -> result.key(index) }) { index, result ->
                val shapes = ListItemDefaults.segmentedShapes(index = index, count = results.size)
                when (result) {
                    is LocationUi.Place -> PlaceItem(
                        name = result.name,
                        address = result.address,
                        shapes = shapes,
                        onClick = { onLocationClick(result) },
                        modifier = Modifier.animateItem(),
                    )
                    is LocationUi.Room -> RoomItem(
                        room = result,
                        shapes = shapes,
                        onClick = { onLocationClick(result) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

/** Places are keyed by position, so the typed one stays the same item while it's being typed. */
private fun LocationUi.key(index: Int): String = when (this) {
    is LocationUi.Place -> "place:$index"
    is LocationUi.Room -> "room:$id"
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            LocationSearch(
                searchState = rememberTextFieldState(),
                results = { previewResults },
                onLocationClick = {},
            )
        }
    }
}

private val previewResults = listOf(
    LocationUi.Place(
        name = "Infomaniak",
        address = "Rue Eugène-Marziano 25, 1227 Les Acacias, Switzerland",
    ),
    LocationUi.Room(
        id = "1",
        name = "Jules Verne room",
        seats = 100,
        floor = 3,
        address = null,
        availability = RoomAvailability.Available,
    ),
    LocationUi.Room(
        id = "2",
        name = "White room",
        seats = 8,
        floor = 1,
        address = null,
        availability = RoomAvailability.Available,
    ),
    LocationUi.Room(
        id = "3",
        name = "Gym",
        seats = 50,
        floor = 0,
        address = null,
        availability = RoomAvailability.Unavailable,
    ),
)
