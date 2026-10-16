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
package com.infomaniak.calendar.components.eventdetail.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Clock
import kotlin.time.Duration

@Stable
data class DateAndTimeState(
    val start: PointInTime,
    val end: PointInTime,
) {
    @Stable
    class PointInTime(
        date: MutableState<LocalDate>,
        time: MutableState<LocalTime>,
        timeZone: MutableState<TimeZone>,
    ) {
        var date by date
            internal set
        var time by time
            internal set
        var timeZone by timeZone
            internal set
    }

    companion object {
        /**
         * Dummy instance just to have something non-null to compute layout sizes on
         */
        val Dummy = DateAndTimeState(
            start = PointInTime(
                date = mutableStateOf(LocalDate(2000, 12, 31)),
                time = mutableStateOf(LocalTime(11, 0)),
                timeZone = mutableStateOf(TimeZone.UTC),
            ),
            end = PointInTime(
                date = mutableStateOf(LocalDate(2000, 12, 31)),
                time = mutableStateOf(LocalTime(11, 0)),
                timeZone = mutableStateOf(TimeZone.UTC),
            ),
        )
    }
}

@Composable
fun rememberDateAndTimeState(defaultEventDuration: Duration, timeZone: TimeZone): DateAndTimeState {
    val (start, end) = computeDefaultValues(timeZone, defaultEventDuration)

    return rememberDateAndTimeState(
        startDate = start.date,
        startTime = start.time,
        startTimeZone = timeZone,
        endDate = end.date,
        endTime = end.time,
        endTimeZone = timeZone,
    )
}

/**
 * Computes default start and end values for a new event. The start is the next full hour, and the end is [eventDuration] after the start.
 */
private fun computeDefaultValues(timeZone: TimeZone, eventDuration: Duration): Pair<LocalDateTime, LocalDateTime> {
    val nowJavaDateTime = Clock.System.now().toLocalDateTime(timeZone).toJavaLocalDateTime()
    val startJava = nowJavaDateTime.truncatedTo(ChronoUnit.HOURS).plusHours(1)

    val start = startJava.toKotlinLocalDateTime()
    val end = start.toInstant(timeZone).plus(eventDuration).toLocalDateTime(timeZone)

    return start to end
}

@Composable
fun rememberDateAndTimeState(
    startDate: LocalDate,
    startTime: LocalTime,
    startTimeZone: TimeZone,
    endDate: LocalDate,
    endTime: LocalTime,
    endTimeZone: TimeZone,
): DateAndTimeState {
    val start = DateAndTimeState.PointInTime(
        date = rememberSaveable { mutableStateOf(startDate) },
        time = rememberSaveable { mutableStateOf(startTime) },
        timeZone = rememberSaveable(stateSaver = TimeZoneSaver) { mutableStateOf(startTimeZone) },
    )

    val end = DateAndTimeState.PointInTime(
        date = rememberSaveable { mutableStateOf(endDate) },
        time = rememberSaveable { mutableStateOf(endTime) },
        timeZone = rememberSaveable(stateSaver = TimeZoneSaver) { mutableStateOf(endTimeZone) },
    )

    return remember { DateAndTimeState(start = start, end = end) }
}

private val TimeZoneSaver = Saver<TimeZone, String>(
    save = { it.id },
    restore = { TimeZone.of(it) },
)
