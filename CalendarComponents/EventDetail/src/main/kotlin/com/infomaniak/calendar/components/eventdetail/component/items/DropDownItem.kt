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
package com.infomaniak.calendar.components.eventdetail.component.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.ChevronsUpDown

@Composable
internal fun DropDownItem(
    content: @Composable () -> Unit,
    trailingContent: @Composable RowScope.() -> Unit,
    onClick: () -> Unit,
    itemCount: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    ConditionallyClickableListItem(
        onClick = onClick,
        contentPadding = contentPadding + ListItemDefaults.ContentPadding,
        content = content,
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                trailingContent()
                if (itemCount > 1) Icon(EsdsIcons.ChevronsUpDown.Small, contentDescription = null)
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ConditionallyClickableListItem(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = ListItemDefaults.ContentPadding,
    content: @Composable () -> Unit,
) {
    if (onClick == null) {
        ListItem(
            contentPadding = contentPadding,
            trailingContent = trailingContent,
            content = content,
            modifier = modifier,
        )
    } else {
        ListItem(
            onClick = onClick,
            contentPadding = contentPadding,
            trailingContent = trailingContent,
            content = content,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DropDownItem(
                    content = { Text("Dropdown item") },
                    trailingContent = { Text("Value") },
                    onClick = {},
                    itemCount = 2,
                )

                DropDownItem(
                    content = { Text("Disabled item") },
                    trailingContent = { Text("Value") },
                    onClick = {},
                    itemCount = 1,
                )
            }
        }
    }
}
