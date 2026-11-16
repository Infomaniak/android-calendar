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
package com.infomaniak.calendar.components.eventdetail.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.PlaceButtonEditable
import com.infomaniak.calendar.components.eventdetail.component.PlaceEditable
import com.infomaniak.calendar.components.eventdetail.component.RoomEditable
import com.infomaniak.calendar.components.eventdetail.component.Section
import com.infomaniak.calendar.components.eventdetail.component.SectionScope
import com.infomaniak.calendar.components.eventdetail.component.item
import com.infomaniak.calendar.components.eventdetail.component.items.ClickableItem
import com.infomaniak.calendar.components.eventdetail.modifier.EventSharedElement
import com.infomaniak.calendar.components.eventdetail.modifier.eventSharedBounds
import com.infomaniak.calendar.components.foundation.models.LocationUi
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.MapPin

/**
 * Shows the picked [location]: a place, or a room above the place it's in, both opening the location search and removed by
 * [onRemoveClick]. Switching between them, e.g. when a calendar switch drops the room, is animated.
 */
internal fun SectionScope.locations(
    location: () -> LocationUi?,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentLocation = location()
    val isRoom = currentLocation is LocationUi.Room
    item(isVisible = isRoom) { shapes ->
        val room = rememberLastNonNull(currentLocation as? LocationUi.Room)

        AnimatedVisibility(
            visible = isRoom,
            modifier = modifier,
        ) {
            room?.let {
                RoomEditable(
                    room = it,
                    onClick = onClick,
                    shapes = shapes,
                    onRemoveClick = onRemoveClick,
                    modifier = Modifier.eventSharedBounds(EventSharedElement.Room),
                )
            }
        }
    }

    val isAddressVisible = isRoom && !currentLocation.address.isNullOrBlank()
    item(isVisible = isAddressVisible) { shapes ->
        val currentLocation = location()
        val room = rememberLastNonNull(currentLocation as? LocationUi.Room)
        val address = room?.address?.takeIf { it.isNotBlank() }

        AnimatedVisibility(visible = isAddressVisible) {
            address?.let {
                PlaceEditable(
                    name = it,
                    address = null,
                    onClick = onClick,
                    shapes = shapes,
                    modifier = Modifier.eventSharedBounds(EventSharedElement.Place),
                )
            }
        }
    }

    val isPlace = currentLocation is LocationUi.Place
    item(isVisible = isPlace) { shapes ->
        val currentLocation = location()
        val place = rememberLastNonNull(currentLocation as? LocationUi.Place)

        AnimatedVisibility(visible = isPlace) {
            place?.let {
                PlaceButtonEditable(
                    name = it.name,
                    address = it.address,
                    onClick = onClick,
                    onRemoveClick = onRemoveClick,
                    shapes = shapes,
                    modifier = Modifier.eventSharedBounds(EventSharedElement.Place),
                )
            }
        }
    }

    val hasLocation = currentLocation == null
    item(isVisible = hasLocation) { shapes ->
        AnimatedVisibility(visible = hasLocation) {
            ClickableItem(
                text = stringResource(R.string.locationOrRoomLabel),
                shapes = shapes,
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

@Preview
@Composable
private fun PreviewEditable() {
    MaterialTheme {
        Surface {
            Section {
                locations(location = { null }, onClick = {}, onRemoveClick = {})
            }
        }
    }
}

@Preview
@Composable
private fun PreviewEditableWithPlace() {
    MaterialTheme {
        Surface {
            Section {
                locations(
                    location = {
                        LocationUi.Place(name = "Infomaniak", address = "Rue Eugène-Marziano 25, 1227 Les Acacias, Switzerland")
                    },
                    onClick = {},
                    onRemoveClick = {},
                )
            }

        }
    }
}

@Preview
@Composable
private fun PreviewEditableWithRoom() {
    MaterialTheme {
        Surface {
            Section {
                locations(
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
}
