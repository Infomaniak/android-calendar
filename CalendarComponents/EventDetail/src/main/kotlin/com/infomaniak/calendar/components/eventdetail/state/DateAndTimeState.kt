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
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

@Stable
data class DateAndTimeState(
    val start: Entry,
    val end: Entry,
    val hasEndEverBeenTouched: MutableState<Boolean>,
) {
    internal fun Entry.pickDate(date: LocalDate) = edit { this.date = date }

    internal fun Entry.pickTime(time: LocalTime) = edit { this.time = time }

    private fun Entry.edit(change: () -> Unit) {
        val previousInterval = end.instant - start.instant

        change()

        val isEditingStart = this == start
        if (isEditingStart.not()) hasEndEverBeenTouched.value = true

        applyCorrections(isEditingStart, previousInterval)
    }

    /** Single place adjusting the other values after the user picked a new one. */
    private fun applyCorrections(isEditingStart: Boolean, previousInterval: Duration) {
        val isOverlapping = start.instant > end.instant

        if (isEditingStart) {
            if (isOverlapping || hasEndEverBeenTouched.value.not()) end.instant = start.instant + previousInterval
        } else {
            if (isOverlapping) start.instant = end.instant - previousInterval
        }
    }

    @Stable
    class Entry(
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

        internal var instant: Instant
            get() = date.atTime(time).toInstant(timeZone)
            set(value) {
                val dateTime = value.toLocalDateTime(timeZone)
                date = dateTime.date
                time = dateTime.time
            }
    }


    companion object {
        /**
         * Dummy instance just to have something non-null to compute layout sizes on
         */
        val Dummy = DateAndTimeState(
            start = Entry(
                date = mutableStateOf(LocalDate(2000, 12, 31)),
                time = mutableStateOf(LocalTime(11, 0)),
                timeZone = mutableStateOf(TimeZone.UTC),
            ),
            end = Entry(
                date = mutableStateOf(LocalDate(2000, 12, 31)),
                time = mutableStateOf(LocalTime(11, 0)),
                timeZone = mutableStateOf(TimeZone.UTC),
            ),
            hasEndEverBeenTouched = mutableStateOf(false),
        )
    }
}

/**
 * Used when editing an existing event. It takes exactly all the data as provided in input.
 */
@Composable
fun rememberDateAndTimeState(
    startDate: LocalDate,
    startTime: LocalTime,
    startTimeZone: TimeZone,
    endDate: LocalDate,
    endTime: LocalTime,
    endTimeZone: TimeZone,
): DateAndTimeState {
    val start = DateAndTimeState.Entry(
        date = rememberSaveable { mutableStateOf(startDate) },
        time = rememberSaveable { mutableStateOf(startTime) },
        timeZone = rememberSaveable(stateSaver = TimeZoneSaver) { mutableStateOf(startTimeZone) },
    )

    val end = DateAndTimeState.Entry(
        date = rememberSaveable { mutableStateOf(endDate) },
        time = rememberSaveable { mutableStateOf(endTime) },
        timeZone = rememberSaveable(stateSaver = TimeZoneSaver) { mutableStateOf(endTimeZone) },
    )

    val hasEndEverBeenTouched = rememberSaveable { mutableStateOf(false) }

    return remember { DateAndTimeState(start = start, end = end, hasEndEverBeenTouched = hasEndEverBeenTouched) }
}

/**
 * Used when creating a new event. It will dynamically compute the right dates to use based on the inputs.
 */
@Composable
fun rememberDateAndTimeState(
    defaultEventDuration: Duration,
    timeZone: TimeZone,
    date: LocalDate = Clock.today(timeZone),
): DateAndTimeState {
    val (start, end) = computeDefaultValues(date, timeZone, defaultEventDuration)

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
 * Computes default start and end values for a new event.
 *
 * The start is the next full hour if the date is today, and the end is [eventDuration] after the start.
 * Otherwise, the start is 8:00 AM on the given date, and the end is [eventDuration] after the start.
 */
private fun computeDefaultValues(
    date: LocalDate,
    timeZone: TimeZone,
    eventDuration: Duration,
): Pair<LocalDateTime, LocalDateTime> {
    val isToday = date == Clock.today(timeZone)

    val (start, end) = if (isToday) {
        Clock.System.now().nextWholeHour(timeZone, eventDuration)
    } else {
        date.atHeightInTheMorning(timeZone, eventDuration)
    }

    return start to end
}

private fun Instant.nextWholeHour(timeZone: TimeZone, eventDuration: Duration): Pair<LocalDateTime, LocalDateTime> {
    val nowJavaDateTime = toLocalDateTime(timeZone).toJavaLocalDateTime()
    val startJava = nowJavaDateTime.truncatedTo(ChronoUnit.HOURS).plusHours(1)

    val start = startJava.toKotlinLocalDateTime()
    val end = start.toInstant(timeZone).plus(eventDuration).toLocalDateTime(timeZone)

    return start to end
}

private fun LocalDate.atHeightInTheMorning(timeZone: TimeZone, eventDuration: Duration): Pair<LocalDateTime, LocalDateTime> {
    val start = atTime(8, 0)
    val end = start.toInstant(timeZone).plus(eventDuration).toLocalDateTime(timeZone)

    return start to end
}

private val TimeZoneSaver = Saver<TimeZone, String>(
    save = { it.id },
    restore = { TimeZone.of(it) },
)
