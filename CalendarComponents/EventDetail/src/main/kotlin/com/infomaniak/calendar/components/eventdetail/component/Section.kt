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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.infomaniak.designsystem.core.theme.EsdsTheme

data class Item(
    val isVisible: Boolean,
    val content: @Composable (ListItemShapes) -> Unit,
)

class SectionScope {
    val itemList = mutableListOf<Item>()
}

fun SectionScope.item(isVisible: Boolean = true, content: @Composable (ListItemShapes) -> Unit) {
    itemList.add(Item(isVisible, content))
}

/**
 * Automatically shows or hides divider based on if any content is composed or not. This layout acts like a column.
 */
@Composable
internal fun Section(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    content: SectionScope.() -> Unit,
) {
    val items = SectionScope().apply(content).itemList
    if (items.isEmpty()) return

    Column(
        modifier = modifier
            .padding(contentPadding)
            .padding(top = EsdsTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        val visibleCount = items.count { it.isVisible }
        var visibleIndex = 0

        items.forEach { item ->
            var previousShapes by remember { mutableStateOf<ListItemShapes?>(null) }

            val shapes = if (item.isVisible) {
                ListItemDefaults.segmentedShapes(index = visibleIndex++, count = visibleCount)
            } else {
                previousShapes ?: ListItemDefaults.segmentedShapes(index = 0, count = 1)
            }

            SideEffect {
                if (item.isVisible) previousShapes = shapes
            }

            item.content(shapes)
        }
    }
}

