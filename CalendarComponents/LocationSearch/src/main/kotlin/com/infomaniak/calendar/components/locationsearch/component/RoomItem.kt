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
package com.infomaniak.calendar.components.locationsearch.component

import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.component.RoomDetails
import com.infomaniak.calendar.components.locationsearch.models.RoomAvailability
import com.infomaniak.calendar.components.locationsearch.models.SearchRoomUi
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.DoorOpen

@Composable
internal fun RoomItem(room: SearchRoomUi, shapes: ListItemShapes, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        colors = LocationItemColors,
        leadingContent = { Icon(EsdsIcons.DoorOpen.Medium, contentDescription = null) },
        overlineContent = {
            Text(
                text = stringResource(room.availability.labelRes),
                color = room.availability.color(),
                style = MaterialTheme.typography.labelMedium,
            )
        },
        supportingContent = { RoomDetails(seats = room.seats, floor = room.floor) },
        modifier = modifier,
    ) {
        Text(room.name)
    }
}

@Preview
@Composable
private fun Preview() {
    Surface {
        RoomItem(
            room = SearchRoomUi(
                id = "1",
                name = "Salle Jules Verne",
                seats = 100,
                floor = 3,
                availability = RoomAvailability.Available,
            ),
            shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewUnavailable() {
    Surface {
        RoomItem(
            room = SearchRoomUi(
                id = "1",
                name = "Salle Hard Rock",
                seats = 6,
                floor = 4,
                availability = RoomAvailability.Unavailable,
            ),
            shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewUnknown() {
    Surface {
        RoomItem(
            room = SearchRoomUi(
                id = "1",
                name = "Salle Jules Verne",
                seats = 100,
                floor = 3,
                availability = RoomAvailability.Unknown,
            ),
            shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            onClick = {},
        )
    }
}
