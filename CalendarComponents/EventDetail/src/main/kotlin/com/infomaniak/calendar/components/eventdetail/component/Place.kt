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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.items.ClickableItem
import com.infomaniak.calendar.components.eventdetail.component.items.ClickableItemDefaults
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIcon
import com.infomaniak.calendar.components.foundation.component.ListItemClickable
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Cross
import com.infomaniak.designsystem.icons.symbols.MapPin
import com.infomaniak.core.common.R as RCommon

@Composable
internal fun PlaceButton(
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

@Composable
internal fun PlaceButtonEditable(
    name: String,
    address: String?,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceButton(
        name = name,
        address = address,
        onClick = onClick,
        modifier = modifier,
        trailingContent = {
            IconButton(onClick = onRemoveClick) {
                Icon(EsdsIcons.Cross.Small, contentDescription = stringResource(RCommon.string.deleteButton))
            }
        },
    )
}

@Composable
internal fun Place(name: String, address: String?, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    ListItemClickable(
        onClick = onClick,
        shapes = ClickableItemDefaults.ClickableItemShapes,
        content = { Text(name) },
        leadingContent = { ItemIcon(EsdsIcons.MapPin) },
        supportingContent = address?.let { { Text(it) } },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun PreviewPlaceButton() {
    MaterialTheme {
        Surface {
            PlaceButton(name = "Infomaniak", address = "Rue Eugène-Marziano 25, 1227 Les Acacias, Switzerland", onClick = {})
        }
    }
}

@Preview
@Composable
private fun PreviewPlaceButtonEditable() {
    MaterialTheme {
        Surface {
            PlaceButtonEditable(
                name = "Infomaniak",
                address = "Rue Eugène-Marziano 25, 1227 Les Acacias, Switzerland",
                onClick = {},
                onRemoveClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewPlace() {
    MaterialTheme {
        Surface {
            Place(name = "Infomaniak", address = "Rue Eugène-Marziano 25, 1227 Les Acacias, Switzerland")
        }
    }
}
