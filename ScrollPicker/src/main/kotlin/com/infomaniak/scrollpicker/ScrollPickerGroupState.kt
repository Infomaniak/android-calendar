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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberScrollPickerGroupState(): ScrollPickerGroupState = remember { ScrollPickerGroupState() }

/**
 * Links pickers whose [ScrollPickerState] share it, e.g. the hours and minutes of a time, so they react as one: while
 * any of them is edited by hand, all of them only show their selected value.
 */
@Stable
class ScrollPickerGroupState {

    private var editingPicker by mutableStateOf<ScrollPickerState?>(null)

    /** Whether a picker of the group is being edited by hand. */
    val isEditing: Boolean get() = editingPicker != null

    /** Keyed by picker, as the next picker may start editing before the previous one reports it stopped. */
    internal fun setEditing(picker: ScrollPickerState, editing: Boolean) {
        if (editing) editingPicker = picker else if (editingPicker == picker) editingPicker = null
    }
}
