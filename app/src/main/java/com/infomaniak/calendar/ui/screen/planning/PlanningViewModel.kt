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
package com.infomaniak.calendar.ui.screen.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.models.YearWeek
import com.infomaniak.calendar.components.planning.PlanningRow
import com.infomaniak.calendar.manager.SyncEventsManager
import com.infomaniak.calendar.manager.SyncEventsManager.SyncPhase
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.core.common.utils.today
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.DotColor
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventColors
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventDaySlice
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class PlanningViewModel(
    accountUtils: AccountUtils,
    private val calendarManager: CalendarManager,
    private val syncEventsManager: SyncEventsManager,
) : ViewModel() {
    val isLoadingEvents: Flow<Boolean> = syncEventsManager.isLoadingEvents

    private val timeZone = TimeZone.currentSystemDefault()
    val today = Clock.today(timeZone)
    private val weekNumbering = WeekNumbering.ISO_8601

    private val emailsByUserId = accountUtils.emailsByUserId.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private val visibleMonth = MutableStateFlow(today.yearMonth)
    private val observedWeek = MutableStateFlow(weekNumbering.weekOf(today))

    /** Recreating this anchor lets an explicit navigation win over a refresh anchored on an old scroll position. */
    private val pagingAnchor = MutableStateFlow(PagingAnchor(day = today, generation = 0))
    private val activePagingSource = AtomicReference<PlanningPagingSource?>()
    private var refreshJob: Job? = null
    private var activeNavigationGeneration: Long? = null
    private var refreshPendingDuringNavigation = false
    private var previousSyncPhase = SyncPhase.Idle

    init {
        observePlanningChanges()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val planningRows: Flow<PagingData<PlanningRow>> = pagingAnchor
        .flatMapLatest { anchor ->
            Pager(
                config = PagingConfig(
                    pageSize = ROWS_PER_PAGE_HINT,
                    // Small on purpose: the 3-week refresh already preloads the immediate neighbours
                    // (see PlanningPagingSource), so this only needs to keep continued scrolling smooth
                    // by loading the next/previous week shortly before reaching an edge.
                    prefetchDistance = PREFETCH_ROWS,
                    // Bound the pages kept in memory: scrolling far drops the farthest weeks (re-loaded
                    // on the way back) so the presented list can't grow unbounded.
                    maxSize = MAX_ROWS_IN_MEMORY,
                    enablePlaceholders = false,
                ),
            ) {
                createPagingSource(anchor.day)
            }.flow
        }
        .cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventDots: StateFlow<Map<LocalDate, List<EventColorsUi>>> = visibleMonth
        .flatMapLatest { month ->
            calendarManager.observeMonthlyDotColors(
                startMonth = month.minus(1, DateTimeUnit.MONTH),
                endMonth = month.plus(1, DateTimeUnit.MONTH),
                timeZone = timeZone,
            )
        }
        .map { it.toEventDots() }
        .stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyMap())

    private fun onVisibleMonthChanged(month: YearMonth) {
        visibleMonth.value = month
    }

    fun onVisibleDateChanged(date: LocalDate) {
        onVisibleMonthChanged(date.yearMonth)
        observedWeek.value = weekNumbering.weekOf(date)
    }

    /**
     * Recenters the planning on [date] by rebuilding the pager. Every request creates a new pager, even
     * when [date] is unchanged, because the active source may have been refreshed around another scroll anchor.
     *
     * The returned generation identifies this navigation until its UI alignment completes.
     */
    fun jumpTo(date: LocalDate): Long {
        val previousAnchor = pagingAnchor.value
        val navigationGeneration = previousAnchor.generation + 1
        cancelRefresh()
        activeNavigationGeneration = navigationGeneration
        pagingAnchor.value = PagingAnchor(day = date, generation = navigationGeneration)
        onVisibleDateChanged(date)
        return navigationGeneration
    }

    fun onNavigationFinished(generation: Long) {
        if (activeNavigationGeneration != generation) return

        activeNavigationGeneration = null
        if (!refreshPendingDuringNavigation) return

        refreshPendingDuringNavigation = false
        requestRefresh(RefreshReason.NavigationSettled)
    }

    private fun createPagingSource(day: LocalDate): PlanningPagingSource {
        return PlanningPagingSource(
            initialDay = day,
            calendarManager = calendarManager,
            emailsByUserId = { emailsByUserId.first() },
            timeZone = timeZone,
            weekNumbering = weekNumbering, //TODO[weekNumbering]: Use week numbering from LocalSettings
            onInvalidated = { source -> activePagingSource.compareAndSet(source, null) },
        ).also { source ->
            activePagingSource.set(source)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePlanningChanges() {
        viewModelScope.launch {
            combine(observedWeek, syncEventsManager.syncPhase) { week, syncPhase -> week to syncPhase }
                .onEach { (_, syncPhase) -> onSyncPhaseChanged(syncPhase) }
                .flatMapLatest { (week, syncPhase) ->
                    if (syncPhase == SyncPhase.Idle) observeWeekChanges(week).drop(1) else emptyFlow()
                }
                .collect { requestRefresh(RefreshReason.LocalChange) }
        }
    }

    private fun observeWeekChanges(week: YearWeek): Flow<Map<LocalDate, List<EventDaySlice>>> {
        return calendarManager.observeDaySlices(
            start = week.firstDay.minus(DAYS_PER_WEEK, DateTimeUnit.DAY).atStartOfDayIn(timeZone),
            end = week.lastDay.plus(DAYS_PER_WEEK + 1, DateTimeUnit.DAY).atStartOfDayIn(timeZone),
            timeZone = timeZone,
        ).distinctUntilChanged()
    }

    private fun onSyncPhaseChanged(syncPhase: SyncPhase) {
        if (syncPhase == previousSyncPhase) return

        when (syncPhase) {
            SyncPhase.DownloadingVisibleRange -> {
                cancelRefresh()
            }
            SyncPhase.SyncingEvents -> {
                if (previousSyncPhase == SyncPhase.DownloadingVisibleRange) {
                    requestRefresh(RefreshReason.VisibleRangeDownloaded)
                }
            }
            SyncPhase.Idle -> {
                when (previousSyncPhase) {
                    SyncPhase.DownloadingVisibleRange -> requestRefresh(RefreshReason.VisibleRangeDownloadFailed)
                    SyncPhase.SyncingEvents -> requestRefresh(RefreshReason.SyncCompleted)
                    SyncPhase.Idle -> Unit
                }
            }
        }
        previousSyncPhase = syncPhase
    }

    private fun requestRefresh(reason: RefreshReason) {
        cancelRefresh()
        if (activeNavigationGeneration != null) {
            refreshPendingDuringNavigation = true
            return
        }
        if (reason == RefreshReason.VisibleRangeDownloaded) {
            activePagingSource.get()?.invalidate()
            return
        }

        refreshJob = viewModelScope.launch {
            delay(REFRESH_SETTLE_DELAY_MILLIS)
            if (syncEventsManager.syncPhase.value != SyncPhase.Idle) {
                return@launch
            }

            activePagingSource.get()?.invalidate()
        }
    }

    private enum class RefreshReason {
        LocalChange,
        VisibleRangeDownloaded,
        VisibleRangeDownloadFailed,
        SyncCompleted,
        NavigationSettled,
    }

    private fun cancelRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    private data class PagingAnchor(val day: LocalDate, val generation: Long)

    private fun Map<LocalDate, List<DotColor>>.toEventDots(): Map<LocalDate, List<EventColorsUi>> {
        return mapValues { (_, colors) -> colors.map { EventColors.from(null, it.sourceColor).toEventColorsUi() } }
    }

    companion object {
        // Weeks have a variable number of rows; these are only hints used by Paging to time prefetch
        // (each source load still returns exactly one week regardless of the requested load size).
        private const val ROWS_PER_PAGE_HINT = 10

        // How many rows from an edge of the loaded list Paging waits before loading the next/previous
        // week. Kept small since the 3-week refresh already preloads the immediate neighbours.
        private const val PREFETCH_ROWS = 6

        // Upper bound on the rows Paging keeps in memory (must be >= pageSize + 2 * prefetchDistance).
        // Roughly a couple of dozen weeks, enough for smooth back-scrolling while capping growth.
        private const val MAX_ROWS_IN_MEMORY = 250

        private const val DAYS_PER_WEEK = 7
        private val REFRESH_SETTLE_DELAY_MILLIS = 250.milliseconds
    }
}
