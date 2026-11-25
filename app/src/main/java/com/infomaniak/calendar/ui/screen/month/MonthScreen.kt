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
package com.infomaniak.calendar.ui.screen.month

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.plus
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infomaniak.calendar.components.calendar.component.HorizontalMonthSelector
import com.infomaniak.calendar.components.calendar.component.rememberCalendarExpansionState
import com.infomaniak.calendar.components.foundation.state.VisibleDayState
import com.infomaniak.calendar.components.foundation.state.rememberVisibleDayState
import com.infomaniak.calendar.components.foundation.utils.NAVIGABLE_MONTHS
import com.infomaniak.calendar.components.month.MonthPager
import com.infomaniak.calendar.components.month.model.MonthUi
import com.infomaniak.calendar.components.month.preview.mockPreviewMonth
import com.infomaniak.calendar.ui.component.OverlaidTopBarScaffold
import com.infomaniak.calendar.ui.component.topAppBar.CalendarTopAppBar
import com.infomaniak.calendar.ui.effects.ApplyJumpRequests
import com.infomaniak.calendar.ui.model.occurrenceId
import com.infomaniak.calendar.ui.state.LocalVisibleDayState
import com.infomaniak.calendar.ui.theme.CalendarThemeForPreview
import com.infomaniak.calendar.ui.theme.Dimens
import com.infomaniak.multiplatform_calendar.core.domain.model.event.OccurrenceId
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth

@Composable
fun MonthScreen(
    goToEventDetail: (occurrenceId: OccurrenceId) -> Unit,
    goToDay: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MonthViewModel = viewModel(),
) {
    val months by viewModel.months.collectAsStateWithLifecycle()
    val isLoadingEvents by viewModel.isLoadingEvents.collectAsStateWithLifecycle(initialValue = false)

    val visibleDayState = LocalVisibleDayState.current ?: return

    ApplyJumpRequests(visibleDayState)

    MonthScreen(
        goToEventDetail = goToEventDetail,
        goToDay = goToDay,
        months = { months },
        isLoadingEvents = { isLoadingEvents },
        visibleDayState = visibleDayState,
        modifier = modifier,
    )
}

@Composable
private fun MonthScreen(
    goToEventDetail: (occurrenceId: OccurrenceId) -> Unit,
    goToDay: () -> Unit,
    months: () -> Map<YearMonth, MonthUi>,
    isLoadingEvents: () -> Boolean,
    visibleDayState: VisibleDayState,
    modifier: Modifier = Modifier,
) {
    val calendarExpansionState = rememberCalendarExpansionState()
    val hazeState = rememberHazeState()

    OverlaidTopBarScaffold(
        topBar = {
            CalendarTopAppBar(
                isLoadingEvents = isLoadingEvents,
                onToggleCalendar = calendarExpansionState::toggle,
                calendarExpansionProgress = { calendarExpansionState.progress },
                hazeState = hazeState,
                calendar = {
                    AnimatedVisibility(
                        visible = calendarExpansionState.isExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        HorizontalMonthSelector(
                            selectedMonth = { visibleDayState.visibleDate.yearMonth },
                            onMonthSelected = { visibleDayState.jumpTo(it.firstDay) },
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        MonthPager(
            monthRange = NAVIGABLE_MONTHS,
            selectedDate = { visibleDayState.visibleDate },
            monthOf = { months()[it] },
            onVisibleMonthChanged = { visibleDayState.updateVisibleDate(it.firstDay) },
            onDayClick = { date ->
                visibleDayState.updateVisibleDate(date)
                goToDay()
            },
            onEventClick = { goToEventDetail(it.occurrenceId) },
            contentPadding = contentPadding + PaddingValues(bottom = Dimens.FloatingToolbarClearance),
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewMonthScreen(months = { mapOf(it to mockPreviewMonth(it)) }, isLoadingEvents = false)
}

@Preview
@Composable
private fun NotLoadedPreview() {
    PreviewMonthScreen(months = { emptyMap() }, isLoadingEvents = true)
}

@Composable
private fun PreviewMonthScreen(months: (visibleMonth: YearMonth) -> Map<YearMonth, MonthUi>, isLoadingEvents: Boolean) {
    CalendarThemeForPreview {
        val visibleDayState = rememberVisibleDayState()

        CompositionLocalProvider(LocalVisibleDayState provides visibleDayState) {
            MonthScreen(
                goToEventDetail = {},
                goToDay = {},
                months = { months(visibleDayState.visibleDate.yearMonth) },
                isLoadingEvents = { isLoadingEvents },
                visibleDayState = visibleDayState,
            )
        }
    }
}
