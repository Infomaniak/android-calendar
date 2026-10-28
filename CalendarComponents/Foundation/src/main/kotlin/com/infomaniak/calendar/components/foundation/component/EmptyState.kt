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
package com.infomaniak.calendar.components.foundation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.Lock
import com.infomaniak.designsystem.icons.symbols.Symbol

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier, icon: Symbol? = null) {
    // TODO: improve empty state
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = EsdsTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(EsdsTheme.spacing.xl, Alignment.CenterVertically),
    ) {
        icon?.let { Icon(icon.ExtraLarge, contentDescription = null) }
        Text(text, style = MaterialTheme.typography.labelMedium, modifier = Modifier.widthIn(max = 300.dp))
    }
}

@Preview
@Composable
private fun PreviewIcon() {
    Surface {
        EmptyState(text = "Empty state", icon = EsdsIcons.Lock)
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    Surface {
        EmptyState(text = "Empty state")
    }
}
