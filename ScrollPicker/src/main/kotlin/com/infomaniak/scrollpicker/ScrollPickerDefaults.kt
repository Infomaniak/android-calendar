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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.designsystem.core.theme.EsdsTheme

object ScrollPickerDefaults {

    val ItemHeight: Dp = 48.dp

    val textStyle: TextStyle
        @Composable get() = MaterialTheme.typography.headlineMediumEmphasized

    val inactiveTextStyle: TextStyle
        @Composable get() = textStyle.copy(fontWeight = FontWeight.Normal)

    /**
     * "inactive" refers to text that's outside the center row.
     */
    @Composable
    fun colors(
        textColor: Color = MaterialTheme.colorScheme.onSurface,
        inactiveTextColor: Color = textColor.copy(alpha = InactiveAlpha),
        pressedTextColor: Color = MaterialTheme.colorScheme.primary,
        inactivePressedTextColor: Color = pressedTextColor.copy(alpha = InactiveAlpha),
        disabledTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledAlpha),
        cursorColor: Color = MaterialTheme.colorScheme.primary,
    ): ScrollPickerColors = ScrollPickerColors(
        textColor = inactiveTextColor,
        selectedTextColor = textColor,
        activeTextColor = inactivePressedTextColor,
        activeSelectedTextColor = pressedTextColor,
        disabledTextColor = disabledTextColor,
        cursorColor = cursorColor,
    )

    val InactiveAlpha @Composable get() = EsdsTheme.opacity.medium
    private const val DisabledAlpha = 0.38f
}
