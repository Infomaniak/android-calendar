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
package com.infomaniak.calendar.components.foundation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenuPopupPositionProvider
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.models.QuickAction

/** Opens a [QuickActionsMenu] of [actions] around [content] when [content] calls the long click it is given. */
@Composable
fun <T : QuickAction> QuickActionsBox(
    actions: List<T>,
    onActionClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    menuModifier: Modifier = Modifier,
    popupPositionProvider: DropdownMenuPopupPositionProvider =
        MenuDefaults.rememberDropdownMenuPopupPositionProvider(MenuAnchorPosition.Below),
    content: @Composable (onLongClick: () -> Unit) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier, propagateMinConstraints = true) {
        content { expanded = true }

        QuickActionsMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            actions = actions,
            onActionClick = onActionClick,
            modifier = menuModifier,
            popupPositionProvider = popupPositionProvider,
        )
    }
}

@Preview
@Composable
private fun QuickActionsBoxPreview() {
    MaterialTheme {
        Surface {
            QuickActionsBox(actions = PreviewQuickAction.entries, onActionClick = {}) { onLongClick ->
                SegmentedListItemClickable(
                    onClick = {},
                    onLongClick = onLongClick,
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
                    content = { Text("Long press me") },
                )
            }
        }
    }
}
