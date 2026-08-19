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
package com.infomaniak.calendar.components.day.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.infomaniak.calendar.components.day.model.MINUTES_PER_HOUR
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

val LocalDateTime.minuteOfDay: Int get() = hour * MINUTES_PER_HOUR + minute

/**
 * Current date and time, re-emitted on every minute boundary.
 *
 * Emitting the whole date-time rather than just the time lets callers notice the day rolling over,
 * so the current time indicator moves to the next day on its own at midnight.
 */
@Composable
fun rememberCurrentDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): State<LocalDateTime> {
    return produceState(initialValue = Clock.System.now().toLocalDateTime(timeZone), timeZone) {
        while (true) {
            val now = Clock.System.now()
            value = now.toLocalDateTime(timeZone)
            delay(now.untilNextMinute())
        }
    }
}

private fun Instant.untilNextMinute() = 1.minutes - (toEpochMilliseconds() % 1.minutes.inWholeMilliseconds).milliseconds
