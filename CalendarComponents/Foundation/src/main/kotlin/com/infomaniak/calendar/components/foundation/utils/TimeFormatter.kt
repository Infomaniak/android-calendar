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
package com.infomaniak.calendar.components.foundation.utils

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Instant
import kotlin.time.toJavaInstant

object TimeFormatter {
    private val format24Hours by lazy { DateTimeFormatter.ofPattern("HH:mm") }
    private val format12Hours by lazy { DateTimeFormatter.ofPattern("hh:mm a") }

    //region Kotlin
    @Composable
    fun Instant.formatHours(): String = toJavaInstant()
        .atZone(ZoneId.systemDefault())
        .format(LocalContext.current.getHourMinuteFormatter())
    //endregion

    //region Java
    @Composable
    fun LocalTime.formatHours(): String = LocalContext.current.getHourMinuteFormatter().format(this)

    @Composable
    fun LocalDateTime.formatHours(): String = toLocalTime().formatHours()
    //endregion

    /**
     * Formats a date according to the user settings for 24 hours format.
     */
    private fun Context.getHourMinuteFormatter(): DateTimeFormatter = if (DateFormat.is24HourFormat(this)) {
        format24Hours
    } else {
        format12Hours
    }
}
