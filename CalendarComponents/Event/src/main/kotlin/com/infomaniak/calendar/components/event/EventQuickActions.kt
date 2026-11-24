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
package com.infomaniak.calendar.components.event

import androidx.collection.intListOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import com.infomaniak.designsystem.core.theme.EsdsTheme

/** Opens the event quick actions menu below [content] when [content] calls the long click it is given. */
@Composable
fun EventQuickActionsBox(
    canEdit: Boolean,
    onQuickActionClick: (EventQuickAction) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (onLongClick: () -> Unit) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier, propagateMinConstraints = true) {
        content { expanded = true }

        EventQuickActions(
            expanded = expanded,
            canEdit = canEdit,
            onDismissRequest = { expanded = false },
            onActionClick = onQuickActionClick,
        )
    }
}

@Composable
private fun EventQuickActions(
    expanded: Boolean,
    canEdit: Boolean,
    onDismissRequest: () -> Unit,
    onActionClick: (EventQuickAction) -> Unit,
) {
    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.padding(top = EsdsTheme.spacing.md),
        popupPositionProvider = MenuDefaults.rememberDropdownMenuPopupPositionProvider(BelowEndAligned),
    ) {
        val actions = listOfNotNull(EventQuickAction.Duplicate, EventQuickAction.Delete.takeIf { canEdit })

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
private fun EventQuickActionsPreview(@PreviewParameter(CanEditPreviewParameter::class) canEdit: Boolean) {
    MaterialTheme {
        Surface {
            EventQuickActions(expanded = true, canEdit = canEdit, onDismissRequest = {}, onActionClick = {})
        }
    }
}

// Candidates are window coordinates, the first one fitting in the window wins
private val BelowEndAligned = MenuAnchorPosition.Custom(
    xCandidates = {
        val endAligned = if (layoutDirection == LayoutDirection.Ltr) anchorBounds.right - menuSize.width else anchorBounds.left
        val startAligned = if (layoutDirection == LayoutDirection.Ltr) anchorBounds.left else anchorBounds.right - menuSize.width
        intListOf(endAligned, startAligned)
    },
    yCandidates = { intListOf(anchorBounds.bottom, anchorBounds.top - menuSize.height) },
)

private class CanEditPreviewParameter : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(true, false)
}
