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
package com.infomaniak.calendar.components.day.paging

import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.day.state.DayColumnsState
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign

/** A fling slower than this, per second, settles where the row is rather than turning the page. */
private val PageTurnVelocityThreshold = 400.dp

/**
 * How far, in days, a position may stray from a page boundary while still counting as on it, which
 * absorbs the rounding of a position read back from pixels.
 */
private const val PAGE_BOUNDARY_TOLERANCE = 0.001f

/**
 * Makes a row of days come to rest on its pages, [daysPerPage] days each, counted from the row's
 * first day: see [pageSnapTarget] for where.
 */
@Composable
internal fun rememberDayPagesFlingBehavior(
    stripState: LazyListState,
    columnsState: DayColumnsState,
    daysPerPage: Int,
): TargetedFlingBehavior {
    val density = LocalDensity.current
    val snapLayoutInfoProvider = remember(stripState, columnsState, daysPerPage, density) {
        DayPagesSnapLayoutInfoProvider(
            stripState = stripState,
            columnsState = columnsState,
            daysPerPage = daysPerPage,
            pageTurnVelocity = with(density) { PageTurnVelocityThreshold.toPx() },
        )
    }

    return rememberSnapFlingBehavior(snapLayoutInfoProvider)
}

private class DayPagesSnapLayoutInfoProvider(
    private val stripState: LazyListState,
    private val columnsState: DayColumnsState,
    private val daysPerPage: Int,
    private val pageTurnVelocity: Float,
) : SnapLayoutInfoProvider {

    /** Left by [calculateApproachOffset] for [calculateSnapOffset], which is not handed it. */
    private var decayOffset = 0f

    /**
     * There is no approach: the target already accounts for where the fling would decay to, and the
     * snap animation carries the fling's velocity all the way there.
     */
    override fun calculateApproachOffset(velocity: Float, decayOffset: Float): Float {
        this.decayOffset = decayOffset
        return 0f
    }

    override fun calculateSnapOffset(velocity: Float): Float {
        val columnWidth = columnsState.columnWidthPx(stripState.layoutInfo.viewportSize.width)
        if (columnWidth == 0) return 0f

        val visibleDayCount = columnsState.visibleDayCount
        val position = stripState.firstVisibleDay(columnWidth)
        val lastPosition = max(0f, stripState.layoutInfo.totalItemsCount - visibleDayCount)

        val target = pageSnapTarget(
            position = position,
            decayTarget = position + decayOffset / columnWidth,
            direction = if (abs(velocity) < pageTurnVelocity) 0 else sign(velocity).toInt(),
            visibleDayCount = visibleDayCount,
            daysPerPage = daysPerPage,
        ).coerceIn(0f, lastPosition)
        decayOffset = 0f

        return (target - position) * columnWidth
    }
}

/**
 * Where a row of days released at [position] comes to rest, positions being counted in days from
 * the start of the first page.
 *
 * - While a page is wider than the viewport, which it is once zoomed in, the row scrolls freely
 *   within the page, a fling stopping at its edge. The next page is only reached by dragging past
 *   that edge, the release then settling on whichever of the two pages it heads to.
 * - Once pages fit in the viewport, the row rests on page starts, like a pager. A fling travels to
 *   the page it would decay to, but never further than a viewport away: when the viewport holds
 *   exactly a page, a fling turns exactly one.
 *
 * @param decayTarget Where the fling would come to rest on its own.
 * @param direction The direction of a fling fast enough to turn the page: 1 forwards, -1 backwards,
 * 0 for a release that only settles.
 */
internal fun pageSnapTarget(
    position: Float,
    decayTarget: Float,
    direction: Int,
    visibleDayCount: Float,
    daysPerPage: Int,
): Float {
    val pageStart = floor((position + PAGE_BOUNDARY_TOLERANCE) / daysPerPage) * daysPerPage
    val nextPageStart = pageStart + daysPerPage
    // Where the viewport has the end of the page at its own end.
    val pageEndInView = nextPageStart - visibleDayCount

    if (pageEndInView > pageStart + PAGE_BOUNDARY_TOLERANCE) {
        return when {
            position <= pageEndInView -> decayTarget.coerceIn(pageStart, pageEndInView)
            direction > 0 -> nextPageStart
            direction < 0 -> pageEndInView
            position - pageEndInView < nextPageStart - position -> pageEndInView
            else -> nextPageStart
        }
    }

    fun floorToPage(day: Float) = floor((day + PAGE_BOUNDARY_TOLERANCE) / daysPerPage) * daysPerPage
    fun ceilToPage(day: Float) = ceil((day - PAGE_BOUNDARY_TOLERANCE) / daysPerPage) * daysPerPage

    return when {
        direction > 0 -> ceilToPage(decayTarget).coerceIn(nextPageStart, max(nextPageStart, floorToPage(position + visibleDayCount)))
        direction < 0 -> floorToPage(decayTarget).coerceIn(min(pageStart, ceilToPage(position - visibleDayCount)), pageStart)
        else -> (position / daysPerPage).roundToInt() * daysPerPage.toFloat()
    }
}
