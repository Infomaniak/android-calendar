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
package com.infomaniak.calendar.components.calendar.modifier

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** How close the selection may get to a bound before the range grows on that side. */
private const val GROWTH_TRIGGER_MARGINS = 1

/** How far past the selection a bound is pushed when growing, so this only happens once per margin. */
private const val GROWTH_TARGET_MARGINS = 1

/**
 * A calendar seen as a pager, so that swiping and selection following can be written once for both
 * the month and the week layouts.
 *
 * A page is identified by the date it opens on, which is the only thing the two layouts have in
 * common: the first of the month for one, the first day of the week for the other.
 */
@Stable
internal interface CalendarPager {

    val scrollableState: ScrollableState

    /** Months held on each side of the selection before the range has to grow. */
    val monthMargin: Int

    /** The page currently on screen. */
    val displayedPage: LocalDate

    /** The dates the layout currently holds. Scrolling outside of it silently does nothing. */
    val pageRange: ClosedRange<LocalDate>

    /** The page [date] belongs to. */
    fun pageOf(date: LocalDate): LocalDate

    /** The page [pageOffset] pages away from [from]. */
    fun pageAt(from: LocalDate, pageOffset: Int): LocalDate

    /** Moves the first bound back to [page]. Prepends pages, which shifts every index. */
    fun growRangeStart(page: LocalDate)

    /** Moves the last bound forward to [page]. Only appends pages, so indices are left alone. */
    fun growRangeEnd(page: LocalDate)

    suspend fun scrollToPage(page: LocalDate, animate: Boolean)

    fun shiftByMargins(page: LocalDate, margins: Int): LocalDate = page.plus(margins * monthMargin, DateTimeUnit.MONTH)
}

/**
 * Widens the range so [page] keeps a margin of pages on both sides.
 *
 * Growing forward leaves the existing indices pointing at the same dates, so it can happen at any
 * time. Growing backward shifts them all by the number of pages added, silently moving the pager
 * onto another page, so it waits for the pager to be still and puts back what was on screen.
 */
internal suspend fun CalendarPager.growRangeAround(page: LocalDate) {
    if (shiftByMargins(page, GROWTH_TRIGGER_MARGINS) > pageRange.endInclusive) {
        growRangeEnd(shiftByMargins(page, GROWTH_TARGET_MARGINS))
    }

    if (shiftByMargins(page, -GROWTH_TRIGGER_MARGINS) < pageRange.start) {
        awaitScroll()

        val wantedStart = shiftByMargins(page, -GROWTH_TARGET_MARGINS)
        if (wantedStart < pageRange.start) {
            val anchor = displayedPage // Read before the indices move under it.
            growRangeStart(wantedStart)
            runCatching { scrollToPage(anchor, animate = false) }
        }
    }
}

internal suspend fun CalendarPager.followSelection(page: LocalDate) {
    awaitScroll()
    snapshotFlow { page in pageRange }.first { it }
    if (page != displayedPage) scrollToPage(page, animate = true)
}

private suspend fun CalendarPager.awaitScroll() {
    snapshotFlow { scrollableState.isScrollInProgress }.first { !it }
}
