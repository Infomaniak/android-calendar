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
package com.infomaniak.calendar.ui.screen.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
fun SettingSection(modifier: Modifier = Modifier, title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        modifier = modifier,
    ) {
        title?.let { SettingTitle(it) }
        content()
    }
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(
                verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xl),
                modifier = Modifier.padding(EsdsTheme.spacing.md),
            ) {
                SettingSection(title = "With title") { PreviewItems(count = 3) }
                SettingSection { PreviewItems(count = 2) }
            }
        }
    }
}

@Composable
private fun PreviewItems(count: Int) {
    repeat(count) { index ->
        SegmentedListItem(
            onClick = {},
            shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        ) {
            Text("Item ${index + 1}")
        }
    }
}
