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

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.infomaniak.calendar.components.eventdetail.component.items.ClickableItemDefaults
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIcon
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Cross
import com.infomaniak.designsystem.icons.symbols.Symbol
import com.infomaniak.core.common.R as RCommon

/** A place's or a room's row, clickable with `ClickableItem`'s shape when [onClick] isn't null. */
@Composable
internal fun LocationItem(
    name: String,
    icon: Symbol,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    supportingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    if (onClick == null) {
        ListItem(
            content = { Text(name) },
            leadingContent = { ItemIcon(icon) },
            supportingContent = supportingContent,
            trailingContent = trailingContent,
            modifier = modifier,
        )
    } else {
        ListItem(
            onClick = onClick,
            shapes = ClickableItemDefaults.ClickableItemShapes,
            content = { Text(name) },
            leadingContent = { ItemIcon(icon) },
            supportingContent = supportingContent,
            trailingContent = trailingContent,
            modifier = modifier,
        )
    }
}

@Composable
internal fun RemoveLocationButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(EsdsIcons.Cross.Small, contentDescription = stringResource(RCommon.string.deleteButton))
    }
}
