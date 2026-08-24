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

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Instant
import kotlin.time.toJavaInstant

object HourFormatter {
    private val format24Hours by lazy { DateTimeFormatter.ofPattern("HH:mm") }
    private val format12Hours by lazy { DateTimeFormatter.ofPattern("hh:mm a") }

    private val labelFormat24Hours by lazy { DateTimeFormatter.ofPattern("HH:mm") }
    private val labelFormat12Hours by lazy { DateTimeFormatter.ofPattern("h a") }

    //region Kotlin
    @Composable
    fun Instant.formatHours(): String = toJavaInstant()
        .atZone(ZoneId.systemDefault())
        .format(selectFormatter(format24Hours, format12Hours))
    //endregion

    //region Java
    @Composable
    fun LocalTime.formatHours(): String = selectFormatter(format24Hours, format12Hours).format(this)

    @Composable
    fun LocalDateTime.formatHours(): String = toLocalTime().formatHours()

    /**
     * Compact form used to label the hour lines of a timeline, such as `06:00` or `6 AM`.
     *
     * Whole hours drop the minutes in 12 hours format, which keeps the label narrow enough for the
     * timeline's hour gutter.
     */
    @Composable
    fun LocalTime.formatHourLabel(): String = selectFormatter(labelFormat24Hours, labelFormat12Hours).format(this)
    //endregion

    /**
     * Picks between the two formats according to the user settings for 24 hours format.
     */
    @Composable
    private fun selectFormatter(format24Hours: DateTimeFormatter, format12Hours: DateTimeFormatter): DateTimeFormatter {
        val currentLocale = LocalLocale.current.platformLocale

        return if (DateFormat.is24HourFormat(LocalContext.current)) {
            format24Hours.withLocale(currentLocale)
        } else {
            format12Hours.withLocale(currentLocale)
        }
    }
}
