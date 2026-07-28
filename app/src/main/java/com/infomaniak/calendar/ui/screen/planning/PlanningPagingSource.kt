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

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.calendar.components.foundation.models.YearWeek
import com.infomaniak.calendar.components.planning.PlanningRow
import com.infomaniak.calendar.components.planning.planningRows
import com.infomaniak.core.common.cancellable
import com.infomaniak.multiplatform_calendar.core.domain.model.account.AccountId
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventDaySlice
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Pages the planning by ISO week, presenting a *flat* [PlanningRow] stream (`WeekHeader` + one row
 * per event). The key is a [YearWeek]; the value is that/those week(s)' rows.
 *
 * The initial **refresh** loads three weeks at once — the target week plus its neighbours — as a
 * single page, so:
 * - jumping onto any day (even a week's last day) can be **top-aligned in one shot**, since the page
 *   already has trailing (and leading) content to scroll against;
 * - the previous/next weeks are **preloaded by construction**, independent of prefetch timing.
 *
 * The leading week matters even for the initial today-centered generation: it keeps the aligned target
 * away from the leading edge, so prefetch doesn't prepend endlessly (which would otherwise scroll the
 * list back to the start of time). Alignment is handled by re-scrolling until settled (see
 * `AlignPlanningToDate`), never by opening the window at the target's week.
 *
 * Subsequent append and prepend loads fetch a single week at a time, keeping page drops fine-grained.
 *
 * [initialDay] resolves the very first refresh key ([initialWeek]). Because
 * [CalendarManager.observeDaySlices] is reactive but a [PagingSource] load is one-shot, each loaded
 * range keeps observing **its own** slice of data and calls [invalidate] on the first change *within
 * that range*, so a change on whichever page the user is looking at reloads the currently loaded pages
 * (mirroring Room's PagingSource behaviour). The number of live observers is bounded by the Pager's
 * `maxSize`.
 */
internal class PlanningPagingSource(
    private val initialDay: LocalDate,
    private val calendarManager: CalendarManager,
    private val emailsByUserId: suspend () -> Map<AccountId, String>,
    private val timeZone: TimeZone,
    private val weekNumbering: WeekNumbering = WeekNumbering.ISO_8601,
) : PagingSource<YearWeek, PlanningRow>() {

    private val initialWeek: YearWeek = weekNumbering.weekOf(initialDay)

    // Hosts the per-loaded-range data observers; cancelled once the source is invalidated.
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        registerInvalidatedCallback { coroutineScope.cancel() }
    }

    override suspend fun load(params: LoadParams<YearWeek>): LoadResult<YearWeek, PlanningRow> {
        return runCatching {
            when (params) {
                is LoadParams.Refresh -> {
                    val center = params.key ?: initialWeek
                    loadWeeks(firstWeek = center.previousWeek(), lastWeek = center.nextWeek())
                }
                is LoadParams.Append -> loadWeeks(firstWeek = params.key, lastWeek = params.key)
                is LoadParams.Prepend -> loadWeeks(firstWeek = params.key, lastWeek = params.key)
            }
        }.cancellable().getOrElse {
            LoadResult.Error(it)
        }
    }

    private suspend fun loadWeeks(firstWeek: YearWeek, lastWeek: YearWeek): LoadResult.Page<YearWeek, PlanningRow> {
        val start = firstWeek.firstDay.atStartOfDayIn(timeZone)
        val endExclusive = lastWeek.lastDay.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

        val slices = calendarManager.observeDaySlices(start = start, end = endExclusive, timeZone = timeZone)
            .distinctUntilChanged()

        // Single subscription per loaded range: the first emission is the current DB snapshot we page
        // now; the first *later, distinct* change to THIS range invalidates so Paging reloads the pages
        // (invalidate() cancels this scope, ending the collection). No drop(1): a range already in the DB
        // only emits once, so dropping it would hang firstSlices.await() and freeze the load (jumps).
        val firstSlices = CompletableDeferred<Map<LocalDate, List<EventDaySlice>>>()
        coroutineScope.launch {
            runCatching {
                slices.collectIndexed { index, value ->
                    if (index == 0) firstSlices.complete(value) else invalidate()
                }
            }.cancellable().onFailure(firstSlices::completeExceptionally)
        }

        val slicesByDay = firstSlices.await()
        val emails = emailsByUserId()
        val data = buildList {
            for (week in weekNumbering.weeksBetween(firstWeek.firstDay, lastWeek.firstDay)) {
                addAll(planningRows(week = week, days = slicesByDay.groupWeekDays(week, emails, timeZone)))
            }
        }

        return LoadResult.Page(
            data = data,
            prevKey = firstWeek.previousWeek(),
            nextKey = lastWeek.nextWeek(),
        )
    }

    override fun getRefreshKey(state: PagingState<YearWeek, PlanningRow>): YearWeek {
        val anchorPosition = state.anchorPosition ?: return initialWeek
        val anchorDate = state.closestItemToPosition(anchorPosition)?.key?.date ?: return initialWeek
        return weekNumbering.weekOf(anchorDate)
    }

    // Recompute through WeekNumbering so the week number stays correct across year boundaries.
    private fun YearWeek.previousWeek(): YearWeek = weekNumbering.weekOf(firstDay.minus(1, DateTimeUnit.DAY))

    private fun YearWeek.nextWeek(): YearWeek = weekNumbering.weekOf(lastDay.plus(1, DateTimeUnit.DAY))
}
