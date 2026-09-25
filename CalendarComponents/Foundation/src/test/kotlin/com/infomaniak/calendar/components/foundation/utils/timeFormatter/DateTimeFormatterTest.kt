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
package com.infomaniak.calendar.components.foundation.utils.timeFormatter

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DateTimeFormatterTest {
    @Before
    fun setUp() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `formatShortNumericDate omits year when it matches current year`() {
        val date = LocalDate(2026, 5, 20)

        assertEquals("5/20", date.formatShortNumericDate(Locale.US, 2026))
    }

    @Test
    fun `formatShortNumericDate includes year when it differs from current year`() {
        val date = LocalDate(2027, 5, 20)

        assertEquals("5/20/2027", date.formatShortNumericDate(Locale.US, 2026))
    }

    @Test
    fun `formatDateTime renders the local date and time for a date this year`() {
        val instant = Instant.parse("2026-05-20T06:00:00Z")
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

        activity.setContent {
            val formatted = instant.formatDateTime(
                timeZone = TimeZone.of("Europe/Paris"),
                currentYear = 2026,
            )
            assertEquals("5/20, 08:00 AM", formatted)
        }
    }

    @Test
    fun `formatDateTime renders the local date and time for a date next year`() {
        val instant = Instant.parse("2027-05-20T06:00:00Z")
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

        activity.setContent {
            val formatted = instant.formatDateTime(
                timeZone = TimeZone.of("Europe/Paris"),
                currentYear = 2026,
            )
            assertEquals("5/20/2027, 08:00 AM", formatted)
        }
    }
}
