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
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.MapPin

@Composable
internal fun PlaceItem(
    name: String,
    address: String?,
    shapes: ListItemShapes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        colors = LocationItemColors,
        leadingContent = { Icon(EsdsIcons.MapPin.Medium, contentDescription = null) },
        supportingContent = address?.let { { Text(it) } },
        modifier = modifier,
    ) {
        Text(name)
    }
}

@Preview
@Composable
private fun Preview() {
    Surface {
        PlaceItem(
            name = "Infomaniak",
            address = "Rue Eugène-Marziano 25, 1227 Les Acacias, Switzerland",
            shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
            onClick = {},
        )
    }
}
