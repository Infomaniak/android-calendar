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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.R
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.ChevronDown

@Composable
fun DrawerSectionHeader(
    organisationName: String?,
    isExpanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded()) 180f else 0f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = isExpanded(), onValueChange = onExpandedChange)
            .padding(horizontal = Margin.Medium, vertical = Margin.Small)
            .semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Margin.Medium),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = organisationName ?: stringResource(R.string.otherCalendarsSectionTitle),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            modifier = Modifier.rotate(rotation),
            imageVector = EsdsIcons.ChevronDown.Small,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun DrawerSectionHeaderPreview() {
    CalendarThemeForPreview {
        DrawerSectionHeader(organisationName = "Infomaniak", isExpanded = { true }, onExpandedChange = {})
    }
}

@Preview
@Composable
private fun DrawerOtherCalendarsSectionHeaderPreview() {
    CalendarThemeForPreview {
        DrawerSectionHeader(organisationName = null, isExpanded = { false }, onExpandedChange = {})
    }
}
