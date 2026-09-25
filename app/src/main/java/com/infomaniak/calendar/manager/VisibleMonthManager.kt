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

import com.infomaniak.calendar.components.foundation.models.EventColorsUi
import com.infomaniak.calendar.ui.screen.planning.toEventColorsUi
import com.infomaniak.core.common.utils.today
import com.infomaniak.multiplatform_calendar.core.domain.model.calendar.DotColor
import com.infomaniak.multiplatform_calendar.core.domain.model.event.EventColors
import com.infomaniak.multiplatform_calendar.core.managers.CalendarManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

@Inject
@SingleIn(AppScope::class)
class VisibleMonthManager(private val calendarManager: CalendarManager) {
    private val timeZone = TimeZone.currentSystemDefault()
    private val visibleMonth = MutableStateFlow(Clock.today(timeZone).yearMonth)

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventDots: Flow<Map<LocalDate, List<EventColorsUi>>> = visibleMonth
        .flatMapLatest { month ->
            calendarManager.observeMonthlyDotColors(
                startMonth = month.minus(MONTH_MARGIN, DateTimeUnit.MONTH),
                endMonth = month.plus(MONTH_MARGIN, DateTimeUnit.MONTH),
                timeZone = timeZone,
            )
        }
        .map { it.toEventDots() }

    fun onVisibleMonthChanged(month: YearMonth) {
        visibleMonth.value = month
    }

    private fun Map<LocalDate, List<DotColor>>.toEventDots(): Map<LocalDate, List<EventColorsUi>> {
        return mapValues { (_, colors) -> colors.map { EventColors.from(null, it.sourceColor).toEventColorsUi() } }
    }

    companion object {
        /** Months kept loaded on each side of the visible one, so a swipe lands on a month that already has its dots. */
        private const val MONTH_MARGIN = 1
    }
}
