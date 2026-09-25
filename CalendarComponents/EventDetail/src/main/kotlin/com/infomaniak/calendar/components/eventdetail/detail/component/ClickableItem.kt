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
package com.infomaniak.calendar.components.eventdetail.detail.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.resources.R

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun ClickableItem(
    text: String,
    leadingIconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    trailingContent: @Composable () -> Unit = ClickableItemDefaults.trailingContent,
    supportingContent: @Composable (() -> Unit)? = null,
) {
    ClickableItem(
        text = text,
        supportingContent = supportingContent,
        leadingContent = { Icon(painterResource(leadingIconRes), contentDescription = null) },
        trailingContent = trailingContent,
        onClick = onClick,
        contentPadding = contentPadding,
        modifier = modifier,
    )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun ClickableItem(
    text: String,
    leadingContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    trailingContent: @Composable () -> Unit = ClickableItemDefaults.trailingContent,
    supportingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        content = { Text(text = text) },
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        onClick = onClick,
        shapes = ListItemDefaults.RectangleShapes,
        contentPadding = contentPadding + ListItemDefaults.ContentPadding,
    )
}

object ClickableItemDefaults {
    val trailingContent = @Composable {
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal val ListItemDefaults.RectangleShapes
    @Composable
    get() = shapes(
        shape = RectangleShape,
        selectedShape = RectangleShape,
        pressedShape = RectangleShape,
        focusedShape = RectangleShape,
        hoveredShape = RectangleShape,
        draggedShape = RectangleShape,
    )

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            ClickableItem(
                text = "Clickable item",
                leadingIconRes = R.drawable.ic_product_kmeet,
                supportingContent = { Text("Supporting content") },
                onClick = {},
            )
        }
    }
}
