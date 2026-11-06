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

/** Colors of a [ScrollPicker] in its different states, see [ScrollPickerDefaults.colors]. */
@Immutable
class ScrollPickerColors(
    val selectedTextColor: Color,
    val unselectedTextColor: Color,
    val disabledSelectedTextColor: Color,
    val disabledUnselectedTextColor: Color,
    val dividerColor: Color,
    val cursorColor: Color,
) {

    internal fun textColor(enabled: Boolean, selected: Boolean): Color = when {
        enabled && selected -> selectedTextColor
        enabled -> unselectedTextColor
        selected -> disabledSelectedTextColor
        else -> disabledUnselectedTextColor
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScrollPickerColors) return false
        return selectedTextColor == other.selectedTextColor &&
                unselectedTextColor == other.unselectedTextColor &&
                disabledSelectedTextColor == other.disabledSelectedTextColor &&
                disabledUnselectedTextColor == other.disabledUnselectedTextColor &&
                dividerColor == other.dividerColor &&
                cursorColor == other.cursorColor
    }

    override fun hashCode(): Int {
        var result = selectedTextColor.hashCode()
        result = 31 * result + unselectedTextColor.hashCode()
        result = 31 * result + disabledSelectedTextColor.hashCode()
        result = 31 * result + disabledUnselectedTextColor.hashCode()
        result = 31 * result + dividerColor.hashCode()
        result = 31 * result + cursorColor.hashCode()
        return result
    }
}
