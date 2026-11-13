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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.items.ClickableItem
import com.infomaniak.calendar.components.eventdetail.component.items.ClickableItemDefaults
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIcon
import com.infomaniak.calendar.components.foundation.models.LocationUi
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Cross
import com.infomaniak.designsystem.icons.symbols.MapPin
import com.infomaniak.core.common.R as RCommon

@Composable
internal fun LocationButton(
    name: String,
    address: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    trailingContent: @Composable () -> Unit = ClickableItemDefaults.trailingContent,
) {
    ClickableItem(
        text = name,
        leadingIcon = EsdsIcons.MapPin,
        onClick = onClick,
        contentPadding = contentPadding,
        supportingContent = address?.let { { Text(it) } },
        modifier = modifier,
        trailingContent = trailingContent,
    )
}

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
    AnimatedContent(
        targetState = location(),
        modifier = modifier,
        contentKey = { it?.let { location -> location::class } },
        transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
        label = "LocationEditable",
    ) { location ->
        when (location) {
            is LocationUi.Place -> LocationButton(
                name = location.name,
                address = location.address,
                onClick = onClick,
                trailingContent = { RemoveLocationButton(onRemoveClick) },
            )
            is LocationUi.Room -> Column(
                Modifier
                    .clip(ClickableItemDefaults.ClickableItemShapes.shape)
                    .clickable(onClick = onClick),
            ) {
                Room(room = location, trailingContent = { RemoveLocationButton(onRemoveClick) })
                location.address?.takeIf { it.isNotBlank() }?.let { address ->
                    ListItem(content = { Text(address) }, leadingContent = { ItemIcon(EsdsIcons.MapPin) })
                }
            }
            null -> ClickableItem(
                text = stringResource(R.string.locationOrRoomLabel),
                leadingIcon = EsdsIcons.MapPin,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun RemoveLocationButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(EsdsIcons.Cross.Small, contentDescription = stringResource(RCommon.string.deleteButton))
    }
}

@Preview
@Composable
private fun PreviewLocationButton() {
    MaterialTheme {
        Surface {
            LocationButton(name = "Infomaniak", address = "Rue Eugène-Marziano 25, 1227 Les Acacias, Switzerland", onClick = {})
        }
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
private fun PreviewEditableWithLocation() {
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
