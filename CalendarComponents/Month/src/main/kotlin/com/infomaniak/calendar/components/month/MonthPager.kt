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
package com.infomaniak.calendar.components.month

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.components.foundation.utils.FollowSelectedPage
import com.infomaniak.calendar.components.month.component.MonthPage
import com.infomaniak.calendar.components.month.model.MonthUi
import com.infomaniak.calendar.components.month.preview.previewFiveWeeksMonth
import com.infomaniak.calendar.components.month.preview.mockPreviewMonth
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.onDay
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

/**
 * Pages through the months of [monthRange], following the month of [selectedDate], which is marked. A month [monthOf]
 * doesn't have yet stays blank until it does.
 */
@Composable
fun MonthPager(
    monthRange: ClosedRange<YearMonth>,
    selectedDate: () -> LocalDate,
    monthOf: (YearMonth) -> MonthUi?,
    onVisibleMonthChanged: (YearMonth) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onEventClick: (EventSummaryUi.Normal) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val pageCount = remember(monthRange) { monthRange.monthCount }
    val selectedMonth = { selectedDate().yearMonth }
    val pagerState = rememberPagerState(initialPage = monthRange.pageOf(selectedMonth())) { pageCount }

    FollowSelectedPage(
        pagerState = pagerState,
        selectedPage = { monthRange.pageOf(selectedMonth()) },
        onPageChanged = { onVisibleMonthChanged(monthRange.monthAt(it)) },
    )

    HorizontalPager(state = pagerState, modifier = modifier) { page ->
        monthOf(monthRange.monthAt(page))?.let { month ->
            MonthPage(
                month = month,
                selectedDate = selectedDate,
                onDayClick = onDayClick,
                onEventClick = onEventClick,
                contentPadding = contentPadding,
            )
        }
    }
}

private val ClosedRange<YearMonth>.monthCount: Int get() = start.monthsUntil(endInclusive) + 1
private fun ClosedRange<YearMonth>.pageOf(month: YearMonth): Int = start.monthsUntil(month)
private fun ClosedRange<YearMonth>.monthAt(page: Int): YearMonth = start.plus(page, DateTimeUnit.MONTH)

@Preview
@Composable
private fun Preview() {
    PreviewMonthPager()
}

@Preview(name = "Right to left")
@Composable
private fun PreviewRtl() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        PreviewMonthPager()
    }
}

@Preview(name = "Not loaded")
@Composable
private fun PreviewNotLoaded() {
    PreviewMonthPager(monthOf = { null })
}

@Composable
private fun PreviewMonthPager(monthOf: (YearMonth) -> MonthUi? = ::mockPreviewMonth) {
    MaterialTheme {
        Surface {
            MonthPager(
                monthRange = previewFiveWeeksMonth.minus(1, DateTimeUnit.MONTH)..
                        previewFiveWeeksMonth.plus(1, DateTimeUnit.MONTH),
                selectedDate = { previewFiveWeeksMonth.onDay(20) },
                monthOf = monthOf,
                onVisibleMonthChanged = {},
                onDayClick = {},
                onEventClick = {},
            )
        }
    }
}
