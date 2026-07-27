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
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.planning.PlanningRow
import com.infomaniak.calendar.di.ViewModelKey
import com.infomaniak.calendar.manager.SyncEventsManager
import com.infomaniak.calendar.utils.account.AccountUtils
import com.infomaniak.core.common.utils.today
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey
class PlanningViewModel(
    accountUtils: AccountUtils,
    private val calendarManager: CalendarManager,
    syncEventsManager: SyncEventsManager,
) : ViewModel() {
    val isLoadingEvents: Flow<Boolean> = syncEventsManager.isLoadingEvents

    private val timeZone = TimeZone.currentSystemDefault()
    val today = Clock.today(timeZone)

    private val emailsByUserId = accountUtils.emailsByUserId.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    /** The day the planning is (re)centered on. Changing it rebuilds the pager around that day. */
    private val initialDay = MutableStateFlow(today)

    @OptIn(ExperimentalCoroutinesApi::class)
    val planningRows: Flow<PagingData<PlanningRow>> = initialDay
        .flatMapLatest { day ->
            Pager(
                config = PagingConfig(
                    pageSize = ROWS_PER_PAGE_HINT,
                    // Kept below one week of rows on purpose: a jump lands the target at least a full
                    // (leading) week below the top, so a small prefetch distance guarantees no prepend
                    // fires right after a jump — which would otherwise run away upward. Neighbouring
                    // weeks are already preloaded by the 3-week refresh (see PlanningPagingSource).
                    prefetchDistance = PREFETCH_ROWS,
                    // Bound the pages kept in memory: scrolling far drops the farthest weeks (re-loaded
                    // on the way back) so the presented list can't grow unbounded.
                    maxSize = MAX_ROWS_IN_MEMORY,
                    enablePlaceholders = false,
                ),
            ) {
                PlanningPagingSource(
                    initialDay = day,
                    calendarManager = calendarManager,
                    emailsByUserId = { emailsByUserId.first() },
                    timeZone = timeZone,
                    weekNumbering = WeekNumbering.ISO_8601, //TODO[weekNumbering]: Use week numbering from LocalSettings
                )
            }.flow
        }
        .cachedIn(viewModelScope)

    /**
     * Recenters the planning on [date] by rebuilding the pager. Returns `true` if this actually changed
     * the center (a rebuild will happen), `false` if [date] was already the center (no-op).
     */
    fun jumpTo(date: LocalDate): Boolean {
        val changed = initialDay.value != date
        initialDay.value = date
        return changed
    }

    companion object {
        // Weeks have a variable number of rows; these are only hints used by Paging to time prefetch
        // (each source load still returns exactly one week regardless of the requested load size).
        private const val ROWS_PER_PAGE_HINT = 10

        // Strictly below the minimum rows of a week (1 header + 7 day rows = 8), so a freshly jumped-to
        // target — always at least a leading week below the top — never sits within the prepend
        // trigger zone. This is what stops a backward jump from scrolling away endlessly.
        private const val PREFETCH_ROWS = 6

        // Upper bound on the rows Paging keeps in memory (must be >= pageSize + 2 * prefetchDistance).
        // Roughly a couple of dozen weeks, enough for smooth back-scrolling while capping growth.
        private const val MAX_ROWS_IN_MEMORY = 250
    }
}
