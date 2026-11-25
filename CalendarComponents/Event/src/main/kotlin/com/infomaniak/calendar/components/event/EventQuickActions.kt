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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import com.infomaniak.calendar.components.foundation.component.QuickActionsBox
import com.infomaniak.calendar.components.foundation.component.QuickActionsMenu
import com.infomaniak.designsystem.core.theme.EsdsTheme

/** Opens the event quick actions menu below [content] when [content] calls the long click it is given. */
@Composable
fun EventQuickActionsBox(
    canEdit: Boolean,
    onQuickActionClick: (EventQuickAction) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (onLongClick: () -> Unit) -> Unit,
) {
    QuickActionsBox(
        actions = eventQuickActions(canEdit),
        onActionClick = onQuickActionClick,
        modifier = modifier,
        menuModifier = Modifier.padding(top = EsdsTheme.spacing.md),
        popupPositionProvider = MenuDefaults.rememberDropdownMenuPopupPositionProvider(BelowEndAligned),
        content = content,
    )
}

private fun eventQuickActions(canEdit: Boolean) = listOfNotNull(EventQuickAction.Duplicate, EventQuickAction.Delete.takeIf { canEdit })

@Preview
@Composable
private fun EventQuickActionsPreview(@PreviewParameter(CanEditPreviewParameter::class) canEdit: Boolean) {
    MaterialTheme {
        Surface {
            QuickActionsMenu(expanded = true, onDismissRequest = {}, actions = eventQuickActions(canEdit), onActionClick = {})
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
