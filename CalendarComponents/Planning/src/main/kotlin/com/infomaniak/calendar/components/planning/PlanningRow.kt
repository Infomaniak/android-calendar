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
package com.infomaniak.calendar.components.planning

import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.YearWeek
import kotlinx.datetime.LocalDate

/**
 * A single flat row of the planning timeline — the value type paged by Paging 3.
 *
 * Keeping the paged type at *row* granularity (rather than a whole week) is what lets a
 * `LazyPagingItems` present one lazy item per event: this preserves per-event virtualization,
 * fine-grained anchor/prefetch, and the sticky day indicator, while the `PagingSource` still loads
 * one ISO week at a time. Each [Event] carries its day's section keys ([Event.daySectionKeys]) so the
 * sticky indicator and section min-height keep working without the surrounding day grouping.
 */
sealed interface PlanningRow {
    val key: PlanningItemKey
    val contentType: Any

    data class WeekHeader(val week: YearWeek) : PlanningRow {
        override val key: PlanningItemKey get() = PlanningItemKey.WeekHeader(week.firstDay)
        override val contentType: Any get() = ContentType.WeekHeader
    }

    data class Event(
        val date: LocalDate,
        val event: EventUi,
        val daySectionKeys: List<PlanningItemKey>,
        val isLastInDay: Boolean,
    ) : PlanningRow {
        override val key: PlanningItemKey get() = PlanningItemKey.Event(date = date, id = event.id)
        override val contentType: Any get() = ContentType.Event
    }

    private enum class ContentType { WeekHeader, Event }
}

/**
 * Flattens a single [week] together with its per-day [EventUi]s into the flat [PlanningRow] stream a
 * `LazyPagingItems` presents: one [PlanningRow.WeekHeader] followed by one [PlanningRow.Event] per
 * event, in [days] iteration order (expected to be day-ascending, all-day first then by start time).
 *
 * Every event of a day shares that day's [PlanningRow.Event.daySectionKeys] so the sticky day
 * indicator can pin itself for the lifetime of the day section.
 */
fun planningRows(week: YearWeek, days: Map<LocalDate, List<EventUi>>): List<PlanningRow> = buildList {
    add(PlanningRow.WeekHeader(week))

    for ((date, events) in days) {
        val sectionKeys = events.map { PlanningItemKey.Event(date = date, id = it.id) }
        events.forEachIndexed { index, event ->
            add(
                PlanningRow.Event(
                    date = date,
                    event = event,
                    daySectionKeys = sectionKeys,
                    isLastInDay = index == events.lastIndex,
                ),
            )
        }
    }
}

