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
package com.infomaniak.calendar.components.eventdetail.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.items.ClickableItemDefaults
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIcon
import com.infomaniak.calendar.components.foundation.component.ListItemClickable
import com.infomaniak.calendar.components.foundation.component.RoomDetails
import com.infomaniak.calendar.components.foundation.models.LocationUi
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Cross
import com.infomaniak.designsystem.icons.symbols.DoorOpen
import com.infomaniak.core.common.R as RCommon

@Composable
internal fun Room(
    room: LocationUi.Room,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    ListItemClickable(
        onClick = onClick,
        shapes = ClickableItemDefaults.ClickableItemShapes,
        content = { Text(room.name) },
        leadingContent = { ItemIcon(EsdsIcons.DoorOpen) },
        supportingContent = { RoomDetails(seats = room.seats, floor = room.floor) },
        trailingContent = trailingContent,
        modifier = modifier,
    )
}

/** [room] above the place it's in, all of it opening the location search and removed by [onRemoveClick]. */
@Composable
internal fun RoomEditable(room: LocationUi.Room, onClick: () -> Unit, onRemoveClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Room(
            room = room,
            onClick = onClick,
            trailingContent = {
                IconButton(onClick = onRemoveClick) {
                    Icon(EsdsIcons.Cross.Small, contentDescription = stringResource(RCommon.string.deleteButton))
                }
            },
        )
    }
}

@Preview
@Composable
private fun PreviewRoom() {
    MaterialTheme {
        Surface {
            Room(room = LocationUi.Room(id = "1", name = "Japan room", seats = 5, floor = 3, address = null, availability = null))
        }
    }
}

@Preview
@Composable
private fun PreviewRoomEditable() {
    MaterialTheme {
        Surface {
            RoomEditable(
                room = LocationUi.Room(
                    id = "1",
                    name = "Jules Verne room",
                    seats = 100,
                    floor = 3,
                    address = "Infomaniak\nRue Eugène-Marziano 25, 1227 Les Acacias",
                    availability = null,
                ),
                onClick = {},
                onRemoveClick = {},
            )
        }
    }
}
