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

import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TimeZoneFormatterTest {

    @Test
    fun `formatLocalizedCityName returns the city in English`() {
        assertEquals("Paris", TimeZone.of("Europe/Paris").formatLocalizedCityName(Locale.US))
    }

    @Test
    fun `formatLocalizedCityName translates the city in the given locale`() {
        assertEquals("Wien", TimeZone.of("Europe/Vienna").formatLocalizedCityName(Locale.GERMAN))
    }

    @Test
    fun `time zone search details are formatted in English`() {
        val zurich = TimeZone.of("Europe/Zurich")
        val summer = Instant.parse("2026-07-01T12:00:00Z")

        assertEquals("Central European Summer Time", zurich.formatLongName(Locale.US, summer))
        assertEquals("Zurich, Switzerland", zurich.formatCityAndCountry(Locale.US))
        assertEquals("GMT+2", zurich.formatUtcOffset(Locale.US, summer))
    }

    @Test
    fun `time zone search details are translated in the given locale`() {
        val zurich = TimeZone.of("Europe/Zurich")
        val summer = Instant.parse("2026-07-01T12:00:00Z")

        assertEquals("heure d’été d’Europe centrale", zurich.formatLongName(Locale.FRENCH, summer))
        assertEquals("Zurich, Suisse", zurich.formatCityAndCountry(Locale.FRENCH))
        assertEquals("UTC+2", zurich.formatUtcOffset(Locale.FRENCH, summer))
    }
}
