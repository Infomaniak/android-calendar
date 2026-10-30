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
package com.infomaniak.calendar.ui.component.drawer

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin

@Composable
fun DrawerSectionHeader(organisationName: String?, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .padding(start = Margin.Medium, top = Margin.Small, end = Margin.Medium, bottom = Margin.Micro)
            .semantics { heading() },
        text = organisationName ?: stringResource(R.string.sectionOtherCalendarsHeader),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true)
@Composable
private fun DrawerSectionHeaderPreview() {
    CalendarThemeForPreview {
        DrawerSectionHeader(organisationName = "Infomaniak")
    }
}

@Preview(showBackground = true)
@Composable
private fun DrawerOtherCalendarsSectionHeaderPreview() {
    CalendarThemeForPreview {
        DrawerSectionHeader(organisationName = null)
    }
}
