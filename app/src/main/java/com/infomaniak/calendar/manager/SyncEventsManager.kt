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
package com.infomaniak.calendar.manager

import androidx.annotation.StringRes
import com.infomaniak.calendar.R
import com.infomaniak.core.common.cancellable
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import com.infomaniak.multiplatform_calendar.data.remote.caldav.RustNetworkException
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import kotlin.time.Duration.Companion.milliseconds
import com.infomaniak.core.common.R as RCore

@SingleIn(AppScope::class)
class SyncEventsManager @Inject constructor(private val calendarManager: CalendarManager) {

    private val _syncPhase = MutableStateFlow(SyncPhase.Idle)
    internal val syncPhase: StateFlow<SyncPhase> = _syncPhase.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val isLoadingEvents: Flow<Boolean> = syncPhase
        .map { it != SyncPhase.Idle }
        .distinctUntilChanged()
        .mapLatest { isLoading ->
            if (isLoading) delay(LOADING_INDICATOR_DELAY)
            isLoading
        }

    private val _loadingError = Channel<SyncError>(Channel.CONFLATED)
    val loadingError: ReceiveChannel<SyncError> = _loadingError

    suspend fun loadCurrentMonths(visibleDate: LocalDate) {
        val timeZone = TimeZone.currentSystemDefault()
        val firstDay = visibleDate.yearMonth.minus(SYNC_WINDOW_MONTHS_BEFORE, DateTimeUnit.MONTH).firstDay
        val lastDay = visibleDate.yearMonth.plus(SYNC_WINDOW_MONTHS_AFTER, DateTimeUnit.MONTH).lastDay.plus(1, DateTimeUnit.DAY)
        _syncPhase.value = SyncPhase.DownloadingVisibleRange
        try {
            if (downloadEventByRange(firstDay, timeZone, lastDay).isFailure) return

            _syncPhase.value = SyncPhase.SyncingEvents
            syncEvents()
        } finally {
            _syncPhase.value = SyncPhase.Idle
        }
    }

    private suspend fun downloadEventByRange(
        firstDay: LocalDate,
        timeZone: TimeZone,
        lastDay: LocalDate,
    ) = runCatching {
        calendarManager.downloadEventsByRange(
            start = firstDay.atStartOfDayIn(timeZone),
            end = lastDay.atStartOfDayIn(timeZone),
        )
    }.cancellable().onFailure {
        _loadingError.trySend(it.toSyncError())
    }

    private suspend fun syncEvents() = runCatching {
        calendarManager.syncEvents()
    }.cancellable().onFailure {
        _loadingError.trySend(it.toSyncError())
    }

    enum class SyncError(@StringRes val errorRes: Int) {
        ErrorRetrieveEvents(errorRes = R.string.syncEventsError),
        ErrorNoConnection(errorRes = RCore.string.connectionError)
    }

    internal enum class SyncPhase {
        Idle,
        DownloadingVisibleRange,
        SyncingEvents,
    }

    private fun Throwable.toSyncError(): SyncError {
        return if (cause is RustNetworkException) {
            SyncError.ErrorNoConnection
        } else {
            SyncError.ErrorRetrieveEvents
        }
    }

    companion object {
        private const val SYNC_WINDOW_MONTHS_BEFORE = 2
        private const val SYNC_WINDOW_MONTHS_AFTER = 3
        private val LOADING_INDICATOR_DELAY = 600.milliseconds
    }
}
