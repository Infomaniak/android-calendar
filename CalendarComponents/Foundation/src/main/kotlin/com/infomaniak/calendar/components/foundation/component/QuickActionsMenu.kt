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

import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.DropdownMenuPopupPositionProvider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.models.QuickAction

/** Shows [actions] in a menu placed by [popupPositionProvider] around its parent layout, and dismisses it once one is clicked. */
@Composable
fun <T : QuickAction> QuickActionsMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    actions: List<T>,
    onActionClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    popupPositionProvider: DropdownMenuPopupPositionProvider =
        MenuDefaults.rememberDropdownMenuPopupPositionProvider(MenuAnchorPosition.Below),
) {
    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        popupPositionProvider = popupPositionProvider,
    ) {
        DropdownMenuGroup(shapes = MenuDefaults.groupShape(index = 0, count = 1)) {
            actions.forEachIndexed { index, action ->
                val textColor = action.contentColor()

                DropdownMenuItem(
                    onClick = {
                        onDismissRequest()
                        onActionClick(action)
                    },
                    text = { Text(stringResource(action.titleRes)) },
                    leadingIcon = { Icon(action.symbol.Small, contentDescription = null) },
                    colors = MenuDefaults.itemColors(textColor = textColor, leadingIconColor = textColor),
                    shape = MenuDefaults.itemShape(index = index, count = actions.size).shape,
                )
            }
        }
    }
}

@Preview
@Composable
private fun QuickActionsMenuPreview() {
    MaterialTheme {
        Surface {
            QuickActionsMenu(
                expanded = true,
                onDismissRequest = {},
                actions = PreviewQuickAction.entries,
                onActionClick = {},
            )
        }
    }
}
