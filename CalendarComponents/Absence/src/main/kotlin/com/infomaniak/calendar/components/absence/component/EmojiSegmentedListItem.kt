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
package com.infomaniak.calendar.components.absence.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infomaniak.designsystem.core.theme.EsdsTheme

@Composable
fun EmojiSegmentedListItem(emoji: String, text: String, shapes: ListItemShapes) {
    SegmentedListItem(
        leadingContent = {
            Text(
                text = emoji,
                modifier = Modifier.size(EsdsTheme.icon.sizeSm),
                // If font has no monospace emojis
                autoSize = TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = 16.sp),
                textAlign = TextAlign.Center,
                // Force line height to not scale with sp so icons stay aligned with other SegmentedItemList
                lineHeight = with(LocalDensity.current) { 20.dp.toSp() },
            )
        },
        content = { Text(text) },
        shapes = shapes,
    )
}

@Preview(name = "Standalone", widthDp = 320)
@Composable
private fun PreviewStandalone() {
    Surface {
        EmojiSegmentedListItem(
            emoji = "🏖️",
            text = "Vacation",
            shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
        )
    }
}

@Preview(name = "Grouped", widthDp = 320)
@Preview(name = "Grouped, large text", widthDp = 320, fontScale = 1.5f)
@Composable
private fun PreviewGrouped() {
    Surface {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            EmojiSegmentedListItem("🏖️", "Vacation", ListItemDefaults.segmentedShapes(index = 0, count = 3))
            EmojiSegmentedListItem("🤒", "Sick leave", ListItemDefaults.segmentedShapes(index = 1, count = 3))
            EmojiSegmentedListItem("👩‍🍼", "Parental leave or family care", ListItemDefaults.segmentedShapes(index = 2, count = 3))
        }
    }
}
