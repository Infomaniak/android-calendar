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
package com.infomaniak.calendar.utils

import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.ui.screen.planning.toEventColorsUi
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.DotColor
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventColors
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Months kept loaded on each side of the visible one, so a swipe lands on a month that already has its dots. */
private const val MONTH_MARGIN = 1

/**
 * Observes the colored dots the calendar draws under its days, for the month [visibleMonth] holds and its neighbours.
 *
 * Shared by every screen showing the calendar above its events, so they all dot their days the same way.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun CalendarManager.observeEventDots(
    visibleMonth: Flow<YearMonth>,
    timeZone: TimeZone,
): Flow<Map<LocalDate, List<EventColorsUi>>> {
    return visibleMonth
        .distinctUntilChanged()
        .flatMapLatest { month ->
            observeMonthlyDotColors(
                startMonth = month.minus(MONTH_MARGIN, DateTimeUnit.MONTH),
                endMonth = month.plus(MONTH_MARGIN, DateTimeUnit.MONTH),
                timeZone = timeZone,
            )
        }
        .map { it.toEventDots() }
}

private fun Map<LocalDate, List<DotColor>>.toEventDots(): Map<LocalDate, List<EventColorsUi>> {
    return mapValues { (_, colors) -> colors.map { EventColors.from(null, it.sourceColor).toEventColorsUi() } }
}
