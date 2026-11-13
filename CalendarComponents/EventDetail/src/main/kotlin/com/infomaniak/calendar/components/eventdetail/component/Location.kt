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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.items.ClickableItem
import com.infomaniak.calendar.components.foundation.models.LocationUi
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Cross
import com.infomaniak.designsystem.icons.symbols.MapPin
import com.infomaniak.core.common.R as RCommon

/**
 * Shows the picked [location]: a place, or a room above the place it's in, both opening the location search and removed by
 * [onRemoveClick]. Switching between them, e.g. when a calendar switch drops the room, is animated.
 */
@Composable
internal fun LocationEditable(
    location: () -> LocationUi?,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val location = location()
    val place = rememberLastNonNull(location as? LocationUi.Place)
    val room = rememberLastNonNull(location as? LocationUi.Room)

    Column(modifier) {
        AnimatedVisibility(visible = location is LocationUi.Place) {
            if (place != null) {
                PlaceButtonEditable(name = place.name, address = place.address, onClick = onClick, onRemoveClick = onRemoveClick)
            }
        }

        AnimatedVisibility(visible = location is LocationUi.Room) {
            if (room != null) RoomEditable(room = room, onClick = onClick, onRemoveClick = onRemoveClick)
        }

        AnimatedVisibility(visible = location == null) {
            ClickableItem(
                text = stringResource(R.string.locationOrRoomLabel),
                leadingIcon = EsdsIcons.MapPin,
                onClick = onClick,
            )
        }
    }
}

/** [value], or the last non-null one while it's null, so a location is still shown while its [AnimatedVisibility] hides it. */
@Composable
private fun <T : Any> rememberLastNonNull(value: T?): T? {
    val last = remember { LastNonNull<T>() }
    if (value != null) last.value = value
    return last.value
}

private class LastNonNull<T : Any> {
    var value: T? = null
}

@Composable
internal fun RemoveLocationButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(EsdsIcons.Cross.Small, contentDescription = stringResource(RCommon.string.deleteButton))
    }
}

@Preview
@Composable
private fun PreviewEditable() {
    MaterialTheme {
        Surface {
            LocationEditable(location = { null }, onClick = {}, onRemoveClick = {})
        }
    }
}

@Preview
@Composable
private fun PreviewEditableWithPlace() {
    MaterialTheme {
        Surface {
            LocationEditable(
                location = {
                    LocationUi.Place(name = "Infomaniak", address = "Rue Eugène-Marziano 25, 1227 Les Acacias, Switzerland")
                },
                onClick = {},
                onRemoveClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewEditableWithRoom() {
    MaterialTheme {
        Surface {
            LocationEditable(
                location = {
                    LocationUi.Room(
                        id = "1",
                        name = "Jules Verne room",
                        seats = 100,
                        floor = 3,
                        address = "Infomaniak\nRue Eugène-Marziano 25, 1227 Les Acacias",
                        availability = null,
                    )
                },
                onClick = {},
                onRemoveClick = {},
            )
        }
    }
}
