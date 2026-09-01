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

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RangeFormatterTest {
    @Test
    fun `time range keeps only the hours`() {
        val formatted = formatTimeRange(
            start = Instant.parse("2026-05-20T08:00:00Z"),
            end = Instant.parse("2026-05-21T09:00:00Z"),
            locale = LOCALE,
            timeZone = TimeZone.UTC,
            use24HourFormat = true,
        )

        assertEquals("08:00 - 09:00", formatted)
    }

    @Test
    fun `same day range shows the date once`() {
        val formatted = formatDateTime(TODAY, LATER_TODAY)
        assertEquals("Wednesday, May 20, 08:00 - 09:00", formatted)
    }

    @Test
    fun `same day range honours the 12 hours format`() {
        val formatted = formatDateTime(TODAY, "2026-05-20T20:30:00+02:00", use24HourFormat = false)
        assertEquals("Wednesday, May 20, 08:00 AM - 08:30 PM", formatted)
    }

    @Test
    fun `range spanning two days repeats the date`() {
        val formatted = formatDateTime(TODAY, TOMORROW)
        assertEquals("Wednesday, May 20, 08:00 - Thursday, May 21, 09:00", formatted)
    }

    @Test
    fun `bounds outside the current year carry it`() {
        val formatted = formatDateTime("2027-05-20T08:00:00+02:00", "2027-05-21T09:00:00+02:00")
        assertEquals("Thursday, May 20, 2027, 08:00 - Friday, May 21, 2027, 09:00", formatted)
    }

    @Test
    fun `range crossing into the next year carries the year on that bound only`() {
        val formatted = formatDateTime("2026-12-31T23:00:00+01:00", "2027-01-01T01:00:00+01:00")
        assertEquals("Thursday, December 31, 23:00 - Friday, January 1, 2027, 01:00", formatted)
    }

    @Test
    fun `date range on a single day shows the date once`() {
        val date = LocalDate(2026, 5, 20)
        assertEquals("Wednesday, May 20", formatDate(date, date))
    }

    @Test
    fun `date range over several days shows both dates`() {
        val formatted = formatDate(LocalDate(2026, 5, 20), LocalDate(2026, 5, 22))
        assertEquals("Wednesday, May 20 - Friday, May 22", formatted)
    }

    @Test
    fun `date range over several years shows years`() {
        val formattedSecondYearDiffers = formatDate(LocalDate(2026, 5, 20), LocalDate(2027, 5, 22))
        assertEquals("Wednesday, May 20 - Saturday, May 22, 2027", formattedSecondYearDiffers)

        val formattedFirstYearDiffers = formatDate(LocalDate(2025, 5, 20), LocalDate(2026, 5, 22))
        assertEquals("Tuesday, May 20, 2025 - Friday, May 22", formattedFirstYearDiffers)
    }

    @Test
    fun `each shipped locale orders and words the range its own way`() {
        val expectedPerLanguage = mapOf(
            "de" to "Mittwoch, 20. Mai, 08:00 - 09:00",
            "es" to "Miércoles, 20 de mayo, 08:00 - 09:00",
            "fi" to "Keskiviikko 20. toukokuuta klo 08:00 - 09:00",
            "fr" to "Mercredi 20 mai, 08:00 - 09:00",
            "nl" to "Woensdag 20 mei 08:00 - 09:00",
            "pt" to "Quarta-feira, 20 de maio, 08:00 - 09:00",
        )

        expectedPerLanguage.forEach { (language, expected) ->
            val formatted = formatDateTime(start = TODAY, end = LATER_TODAY, locale = Locale.forLanguageTag(language))

            assertEquals(language, expected, formatted)
        }
    }

    @Test
    fun `zoned range on a single day only displays the hours and collapses the zone indicator`() {
        val formatted = formatZoned()
        assertEquals("08:00 - 09:00 (GMT+2)", formatted)
    }

    @Test
    fun `zoned range repeats the date and offset when it spans several days`() {
        val formatted = formatZoned(end = TOMORROW)
        assertEquals("Wednesday, May 20, 08:00 (GMT+2) - Thursday, May 21, 09:00 (GMT+2)", formatted)
    }

    @Test
    fun `each bound carries its own offset`() {
        val formatted = formatZoned(endTimeZone = TimeZone.of("Asia/Tokyo"))
        assertEquals("08:00 (GMT+2) - 16:00 (GMT+9)", formatted)
    }

    @Test
    fun `bounds inside the repeated hour of a DST overlap keep the offset they happened at`() {
        val formatted = formatZoned(
            start = "2026-10-25T00:30:00Z", // 02:30 in Paris, first pass through the hour it repeats
            end = "2026-10-25T01:30:00Z", // 02:30 in Paris again, an hour later
        )

        assertEquals("02:30 (GMT+2) - 02:30 (GMT+1)", formatted)
    }

    @Test
    fun `zoned range words the offset the way each locale does`() {
        val expectedPerLanguage = mapOf(
            "de" to "Mittwoch, 20. Mai, 08:00 (GMT+2) - Donnerstag, 21. Mai, 09:00 (GMT+2)",
            "fi" to "Keskiviikko 20. toukokuuta klo 08:00 (UTC+2) - Torstai 21. toukokuuta klo 09:00 (UTC+2)",
            "fr" to "Mercredi 20 mai, 08:00 (UTC+2) - Jeudi 21 mai, 09:00 (UTC+2)",
            "nl" to "Woensdag 20 mei 08:00 (GMT+2) - Donderdag 21 mei 09:00 (GMT+2)",
        )

        expectedPerLanguage.forEach { (language, expected) ->
            assertEquals(language, expected, formatZoned(end = TOMORROW, locale = Locale.forLanguageTag(language)))
        }
    }

    companion object {
        private val LOCALE = Locale.ENGLISH
        private val PARIS = TimeZone.of("Europe/Paris")
        private const val CURRENT_YEAR = 2026

        private const val TODAY = "2026-05-20T08:00:00+02:00"
        private const val LATER_TODAY = "2026-05-20T09:00:00+02:00"
        private const val TOMORROW = "2026-05-21T09:00:00+02:00"

        private fun formatDateTime(
            start: String,
            end: String,
            use24HourFormat: Boolean = true,
            locale: Locale = LOCALE,
        ): String = formatDateTimeRange(
            start = Instant.parse(start).toLocalDateTime(PARIS),
            end = Instant.parse(end).toLocalDateTime(PARIS),
            locale = locale,
            use24HourFormat = use24HourFormat,
            currentYear = CURRENT_YEAR,
        )

        private fun formatDate(startDate: LocalDate, endDate: LocalDate): String {
            return formatDateRange(startDate, endDate, LOCALE, CURRENT_YEAR)
        }

        private fun formatZoned(
            locale: Locale = LOCALE,
            start: String = TODAY,
            end: String = LATER_TODAY,
            startTimeZone: TimeZone = PARIS,
            endTimeZone: TimeZone = PARIS,
        ): String = formatDateTimeRangeWithZone(
            start = Instant.parse(start),
            startTimeZone = startTimeZone,
            end = Instant.parse(end),
            endTimeZone = endTimeZone,
            locale = locale,
            use24HourFormat = true,
            currentYear = CURRENT_YEAR,
        )
    }
}
