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
package com.infomaniak.calendar.ui.navigation

import com.infomaniak.calendar.ui.navigation.NavDestination.CalendarView
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Guards the [CalendarView.storageKey] values, which are persisted on disk and must therefore never change, even when a
 * destination gets renamed.
 */
class CalendarViewStorageKeyTest {

    @Test
    fun `storage keys never change`() {
        allCalendarViews.forEach { calendarView ->
            assertEquals(
                "The storage key of $calendarView is persisted on disk and must never change",
                calendarView.expectedStorageKey,
                calendarView.storageKey,
            )
        }
    }

    @Test
    fun `every storage key maps back to its own destination`() {
        allCalendarViews.forEach { calendarView ->
            assertEquals(calendarView, CalendarView.fromStorageKey(calendarView.expectedStorageKey))
        }
    }

    @Test
    fun `an unknown storage key falls back to the default destination`() {
        assertEquals(CalendarView.Default, CalendarView.fromStorageKey("someRemovedView"))
    }

    private companion object {
        val allCalendarViews = listOf(
            CalendarView.Planning,
            CalendarView.Day,
            CalendarView.ThreeDays,
            CalendarView.Week,
            CalendarView.Month,
        )

        /**
         * Exhaustive `when` on purpose: adding a new [CalendarView] doesn't compile until its expected key is declared here, and
         * renaming an existing one can't silently change the key already stored on users' devices.
         */
        val CalendarView.expectedStorageKey: String
            get() = when (this) {
                CalendarView.Planning -> "planning"
                CalendarView.Day -> "day"
                CalendarView.ThreeDays -> "threeDays"
                CalendarView.Week -> "week"
                CalendarView.Month -> "month"
            }
    }
}
