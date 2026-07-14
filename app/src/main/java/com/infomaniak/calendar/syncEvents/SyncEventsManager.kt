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
package com.infomaniak.calendar.syncEvents

import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import kotlin.time.Duration.Companion.milliseconds

class SyncEventsManager @Inject constructor(private val calendarManager: CalendarManager) {

    suspend fun loadCurrentMonths(visibleDate: LocalDate, onLoadingChanged: (Boolean) -> Unit) {
        val timeZone = TimeZone.currentSystemDefault()
        val firstDay = visibleDate.yearMonth.minus(SYNC_WINDOW_MONTHS_BEFORE, DateTimeUnit.MONTH).firstDay
        val lastDay = visibleDate.yearMonth.plus(SYNC_WINDOW_MONTHS_AFTER, DateTimeUnit.MONTH).lastDay.plus(1, DateTimeUnit.DAY)

        withDelayedLoadingIndicator(onLoadingChanged) {
            calendarManager.downloadEventsByRange(
                start = firstDay.atStartOfDayIn(timeZone),
                end = lastDay.atStartOfDayIn(timeZone),
            )
        }

        calendarManager.syncEvents()
    }

    private suspend fun withDelayedLoadingIndicator(onLoadingChanged: (Boolean) -> Unit, block: suspend () -> Unit) {
        coroutineScope {
            val delayedLoadingJob = launch {
                delay(LOADING_INDICATOR_DELAY)
                onLoadingChanged(true)
            }

            try {
                block()
            } finally {
                delayedLoadingJob.cancel()
                onLoadingChanged(false)
            }
        }
    }

    companion object {
        private const val SYNC_WINDOW_MONTHS_BEFORE = 2
        private const val SYNC_WINDOW_MONTHS_AFTER = 3
        private val LOADING_INDICATOR_DELAY = 600.milliseconds
    }
}
