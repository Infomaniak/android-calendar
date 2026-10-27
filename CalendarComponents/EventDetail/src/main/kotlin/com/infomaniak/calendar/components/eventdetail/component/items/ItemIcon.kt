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
package com.infomaniak.calendar.components.eventdetail.component.items

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.eventdetail.component.items.ItemIconDefaults.getSymbolSize
import com.infomaniak.designsystem.core.theme.EsdsTheme
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.ProductKmeet
import com.infomaniak.designsystem.icons.symbols.Symbol

@Composable
internal fun ItemIcon(
    symbol: Symbol,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Icon(getSymbolSize(symbol), null, modifier, tint)
}

@Composable
internal fun ItemIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Icon(imageVector, null, modifier.size(ItemIconDefaults.size), tint)
}

internal object ItemIconDefaults {
    // Keep these two sizes in sync
    val size @Composable get() = EsdsTheme.icon.sizeSm
    val getSymbolSize = Symbol::Small
}

@Preview(name = "ItemIcon")
@Composable
private fun PreviewItemIcon() {
    ItemIcon(EsdsIcons.ProductKmeet)
}
