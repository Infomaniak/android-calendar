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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
fun SettingTitle(text: String, modifier: Modifier = Modifier.Companion) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier
            .semantics { heading() }
            .padding(horizontal = EsdsTheme.spacing.xl)
            .padding(top = EsdsTheme.spacing.threeXl, bottom = EsdsTheme.spacing.md),
    )
}

@Preview
@Composable
private fun Preview() {
    CalendarThemeForPreview {
        SettingTitle("Section title")
    }
}

@Preview
@Composable
private fun InContextPreview() {
    CalendarThemeForPreview {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(modifier = Modifier.padding(horizontal = EsdsTheme.spacing.md)) {
                PreviewSection(title = "First section", itemCount = 3)
                PreviewSection(title = "Second section", itemCount = 2)
            }
        }
    }
}

@Composable
private fun PreviewSection(title: String, itemCount: Int) {
    Column {
        SettingTitle(title)
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            repeat(itemCount) { index ->
                SegmentedListItem(
                    onClick = {},
                    shapes = ListItemDefaults.segmentedShapes(index = index, count = itemCount),
                ) {
                    Text("Item ${index + 1}")
                }
            }
        }
    }
}
