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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import com.infomaniak.calendar.components.eventdetail.modifier.EventSharedElement
import com.infomaniak.calendar.components.eventdetail.modifier.eventSharedElement
import com.infomaniak.designsystem.core.theme.EsdsTheme

class SectionScope {
    val itemList = mutableListOf<@Composable (ListItemShapes) -> Unit>()
}

fun SectionScope.item(content: @Composable (ListItemShapes) -> Unit) {
    itemList.add(content)
}

/**
 * Automatically shows or hides divider based on if any content is composed or not. This layout acts like a column.
 */
@Composable
internal fun Section(
    sharedElementId: EventSharedElement,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    content: SectionScope.() -> Unit,
) {
    val scope = SectionScope().apply(content)
    if (scope.itemList.isNotEmpty()) {
        scope.itemList.forEachIndexed { index, function ->
            function(ListItemDefaults.segmentedShapes(index, scope.itemList.size))
        }
    }
}

@Composable
private fun Divider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.padding(vertical = EsdsTheme.spacing.md))
}
