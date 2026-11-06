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
package com.infomaniak.scrollpicker

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ScrollPickerDefaults {

    const val VisibleItemCount = 5
    val ItemHeight: Dp = 48.dp

    val textStyle: TextStyle
        @Composable get() = MaterialTheme.typography.headlineMedium

    @Composable
    fun colors(
        selectedTextColor: Color = MaterialTheme.colorScheme.primary,
        unselectedTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledSelectedTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledAlpha),
        disabledUnselectedTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledAlpha),
        cursorColor: Color = MaterialTheme.colorScheme.primary,
    ): ScrollPickerColors = ScrollPickerColors(
        selectedTextColor = selectedTextColor,
        unselectedTextColor = unselectedTextColor,
        disabledSelectedTextColor = disabledSelectedTextColor,
        disabledUnselectedTextColor = disabledUnselectedTextColor,
        cursorColor = cursorColor,
    )

    private const val DisabledAlpha = 0.38f
}
