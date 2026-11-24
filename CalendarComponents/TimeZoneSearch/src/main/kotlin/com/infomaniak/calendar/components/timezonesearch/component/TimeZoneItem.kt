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
package com.infomaniak.calendar.components.timezonesearch.component

import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.component.HighlightedText
import com.infomaniak.calendar.components.foundation.theme.ComponentColors
import com.infomaniak.calendar.components.timezonesearch.models.TimeZoneUi
import kotlinx.datetime.TimeZone

@Composable
internal fun TimeZoneItem(
    timeZone: TimeZoneUi,
    query: () -> String,
    shapes: ListItemShapes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        shapes = shapes,
        colors = ComponentColors.SegmentedListItemColors,
        onClick = onClick,
        overlineContent = { HighlightedText(timeZone.name, query) },
        content = { HighlightedText(timeZone.location, query) },
        supportingContent = { HighlightedText(timeZone.utcOffset, query) },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    Surface {
        TimeZoneItem(
            timeZone = TimeZoneUi(TimeZone.of("Europe/Zurich")),
            query = { "zur" },
            shapes = ListItemDefaults.segmentedShapes(0, 1),
            onClick = {},
        )
    }
}
