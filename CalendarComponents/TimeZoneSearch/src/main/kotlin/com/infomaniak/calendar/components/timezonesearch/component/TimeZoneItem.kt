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

import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.component.HighlightedText
import com.infomaniak.calendar.components.timezonesearch.models.TimeZoneUi
import kotlinx.datetime.TimeZone

@Composable
internal fun TimeZoneItem(timeZone: TimeZoneUi, query: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
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
            query = "zur",
            onClick = {},
        )
    }
}
