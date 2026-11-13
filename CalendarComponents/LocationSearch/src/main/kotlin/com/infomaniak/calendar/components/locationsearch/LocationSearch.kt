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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.component.SearchBar
import com.infomaniak.calendar.components.foundation.component.SearchLayout
import com.infomaniak.calendar.components.foundation.models.SearchableText
import com.infomaniak.calendar.components.foundation.utils.onlyHorizontalAndTop
import com.infomaniak.calendar.components.locationsearch.component.PlaceItem
import com.infomaniak.calendar.components.locationsearch.component.RoomItem
import com.infomaniak.calendar.components.locationsearch.models.SearchRoomUi
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.core.theme.EsdsTheme

/**
 * [searchState] holds the typed location, listed first so it can be picked like the rooms below it.
 * Picking the typed location reports its text through [onLocationClick], picking a room reports it through [onRoomClick].
 */
@Composable
fun LocationSearch(
    searchState: TextFieldState,
    rooms: List<SearchRoomUi>,
    onLocationClick: (String) -> Unit,
    onRoomClick: (SearchRoomUi) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val typedLocation by remember(searchState) { derivedStateOf { searchState.text.trim().toString() } }
    val searchableRooms = remember(rooms) { rooms.associateWith { SearchableText(it.name) } }
    val visibleRooms by remember(searchableRooms) {
        derivedStateOf {
            searchableRooms.mapNotNull { (room, name) -> room.takeIf { typedLocation.isEmpty() || name.find(typedLocation) != null } }
        }
    }

    SearchLayout(modifier = modifier, contentPadding = contentPadding.onlyHorizontalAndTop()) {
        SearchBar(searchState, placeholder = stringResource(R.string.locationSearchPlaceholder))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + EsdsTheme.spacing.sm),
        ) {
            val typedCount = if (typedLocation.isEmpty()) 0 else 1
            val count = typedCount + visibleRooms.size

            if (typedCount > 0) item(key = "typed") {
                PlaceItem(
                    place = typedLocation,
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = count),
                    onClick = { onLocationClick(typedLocation) },
                )
            }

            itemsIndexed(visibleRooms, key = { _, room -> "room:${room.id}" }) { index, room ->
                RoomItem(
                    room = room,
                    shapes = ListItemDefaults.segmentedShapes(index = typedCount + index, count = count),
                    onClick = { onRoomClick(room) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            LocationSearch(
                searchState = rememberTextFieldState(),
                rooms = listOf(
                    SearchRoomUi(id = "1", name = "Salle Jules Verne", seats = 100, floor = 3),
                    SearchRoomUi(id = "2", name = "Salle Blanche", seats = 8, floor = 1),
                    SearchRoomUi(id = "3", name = "Salle de sport", seats = 50, floor = 0),
                ),
                onLocationClick = {},
                onRoomClick = {},
            )
        }
    }
}
