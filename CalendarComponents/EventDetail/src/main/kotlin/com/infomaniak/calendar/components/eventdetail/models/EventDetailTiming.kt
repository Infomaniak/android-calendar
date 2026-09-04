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
package com.infomaniak.calendar.components.eventdetail.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.infomaniak.calendar.components.foundation.state.rememberCurrentTimeZone
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Immutable
sealed interface EventDetailTiming {
    val atTimeZone: LocalDateTime
    @get:Composable
    val atLocale: LocalDateTime

    @Immutable
    data class Precised(private val _instant: Instant, val timeZone: TimeZone) : EventDetailTiming {
        override val atTimeZone: LocalDateTime = _instant.toLocalDateTime(timeZone)
        override val atLocale: LocalDateTime @Composable get() = _instant.toLocalDateTime(rememberCurrentTimeZone().value)

        val utcOffsetAtTimeZone: UtcOffset = timeZone.offsetAt(_instant)
        val utcOffsetAtLocale: UtcOffset @Composable get() = rememberCurrentTimeZone().value.offsetAt(_instant)
    }

    @Immutable
    class Floating(val date: LocalDateTime) : EventDetailTiming {
        override val atTimeZone: LocalDateTime = date
        override val atLocale: LocalDateTime @Composable get() = date
    }
}
