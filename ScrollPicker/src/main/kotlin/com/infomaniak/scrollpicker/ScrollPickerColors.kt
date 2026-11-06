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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colors of a [ScrollPicker] in its different states, see [ScrollPickerDefaults.colors]. A picker draws its items with
 * [activeTextColor] while it's scrolled or edited and [textColor] otherwise, and the part of them inside the selection
 * with [activeSelectedTextColor] and [selectedTextColor] respectively.
 */
@Immutable
data class ScrollPickerColors(
    val textColor: Color,
    val selectedTextColor: Color,
    val activeTextColor: Color,
    val activeSelectedTextColor: Color,
    val disabledTextColor: Color,
    val cursorColor: Color,
) {

    internal fun textColor(enabled: Boolean, active: Boolean, selected: Boolean): Color = when {
        !enabled -> disabledTextColor
        active -> if (selected) activeSelectedTextColor else activeTextColor
        else -> if (selected) selectedTextColor else textColor
    }
}
