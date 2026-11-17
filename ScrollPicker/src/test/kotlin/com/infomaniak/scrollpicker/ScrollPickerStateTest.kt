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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrollPickerStateTest {

    @Test
    fun selectedItemFollowsTheWheelRowAcrossConfigurationChanges() {
        val state = ScrollPickerState(initialIndex = 8)
        assertEquals(8, state.selectedIndex)

        assertEquals(10, state.setItems(itemCount = 10, loop = false))
        state.select(2)
        assertEquals(2, state.selectedListIndex)
        assertEquals(2, state.selectedIndex)

        assertEquals(100_000, state.setItems(itemCount = 10, loop = true))
        state.select(9)
        assertEquals(9, state.itemIndexOf(state.selectedListIndex))
        assertEquals(9, state.selectedIndex)

        val lastRow = state.selectedListIndex
        state.select(0)
        assertEquals(lastRow + 1, state.selectedListIndex)
        assertEquals(0, state.selectedIndex)
        state.select(9)

        state.setItems(itemCount = 4, loop = true)
        assertEquals(3, state.selectedIndex)
        assertEquals(3, state.itemIndexOf(state.selectedListIndex))

        state.setItems(itemCount = 4, loop = false)
        assertEquals(3, state.selectedListIndex)
        assertEquals(3, state.selectedIndex)

        state.setItems(itemCount = 0, loop = true)
        assertEquals(0, state.selectedIndex)
    }

    @Test
    fun selectionSetFromCodeIsSelectedBeforeTheWheelGetsThere() {
        val state = ScrollPickerState(initialIndex = 2)
        state.setItems(itemCount = 10, loop = true)
        val wheelRow = state.selectedListIndex

        state.selectedIndex = 7
        assertEquals(7, state.selectedIndex)
        assertEquals(wheelRow, state.selectedListIndex)

        state.select(4)
        assertEquals(wheelRow + 2, state.selectedListIndex)
        assertEquals(4, state.selectedIndex)
    }

    @Test
    fun previousPickerLosingFocusDoesNotEndTheNewPickersEditing() {
        val group = ScrollPickerGroupState()
        val first = ScrollPickerState(group = group)
        val second = ScrollPickerState(group = group)

        group.setEditing(first, editing = true)
        group.setEditing(second, editing = true)
        group.setEditing(first, editing = false)
        assertTrue(group.isEditing)
        assertFalse(first.isEditing)
        assertTrue(second.isEditing)

        group.setEditing(second, editing = false)
        assertFalse(group.isEditing)
    }
}
