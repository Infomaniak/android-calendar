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
package com.infomaniak.calendar.components.calendar.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.models.WeekNumbering
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinDayOfWeek
import kotlin.time.Clock

/** Roughly eight years each way, far enough that the range never needs to be recentered. */
private const val RANGE_MONTHS = 100

/**
 * A calendar that switches between a single week ([CollapsedCalendar]) and a full month
 * ([ExpandedCalendar]), with the days shared between the two forms so they slide into place rather
 * than cross-fading.
 *
 * The days-of-week header is not rendered by either calendar: they only reserve its space, and the
 * real one is drawn on top by [DayOfWeekOverlayHeader]. See that function for why.
 *
 * @param isExpanded which form to display, as a lambda so the read stays as low as possible.
 * @param selectedDate the highlighted day, forwarded as a lambda all the way down to the day cells
 * so a selection change only recomposes the two cells it affects.
 * @param onDayClick called when a day is picked, whether by tapping it or by swiping to another
 * page (in which case the first day of that page is reported).
 */
@Composable
fun ExpandableCalendar(
    isExpanded: () -> Boolean,
    selectedDate: () -> LocalDate,
    onDayClick: (LocalDate) -> Unit,
    weekNumbering: WeekNumbering,
    modifier: Modifier = Modifier,
) {
    val firstDayOfWeek = remember(weekNumbering) { weekNumbering.firstDayOfWeek.toKotlinDayOfWeek() }

    // Shared by both calendars: each one publishes its own scroll offset into its own slot, so the
    // two never overwrite each other while both are on screen during the expand/collapse animation.
    val headerState = rememberCalendarHeaderState()

    var headerWidth by remember { mutableIntStateOf(0) }

    Box(modifier = modifier) {
        // Gives the day cells a shared element scope: a day present in both forms (the days of the
        // selected week) is matched by date and animates from its week position to its month
        // position instead of fading out and back in.
        SharedTransitionLayout {
            AnimatedContent(
                targetState = isExpanded(),
                label = "calendarExpansion",
            ) { expanded ->
                if (expanded) {
                    Column {
                        ExpandedCalendar(
                            selectedDate = selectedDate,
                            onDayClick = onDayClick,
                            // Swiping to another month selects its first day.
                            onVisibleMonthChange = { onDayClick(it.firstDay) },
                            weekNumbering = weekNumbering,
                            monthRange = RANGE_MONTHS,
                            headerState = headerState,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@AnimatedContent,
                        )
                    }
                } else {
                    CollapsedCalendar(
                        selectedDate = selectedDate,
                        onDayClick = onDayClick,
                        // The reported date is already the first day of the week.
                        onVisibleWeekChange = { onDayClick(it) },
                        weekNumbering = weekNumbering,
                        monthRange = RANGE_MONTHS,
                        headerState = headerState,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
                    )
                }
            }
        }

        DayOfWeekOverlayHeader(
            headerWidth = { headerWidth },
            updateHeaderWidth = { headerWidth = it },
            firstDayOfWeek = firstDayOfWeek,
            headerState = headerState,
            isExpanded = isExpanded,
        )
    }
}

/**
 * Draws the days-of-week row on top of the calendars.
 *
 * Each calendar already renders this row inside its scrolling content, but fully transparent: that
 * copy exists only to reserve the right amount of space and to keep the header area part of the
 * swipe surface. Drawing the visible one here instead means it survives the expand/collapse
 * transition untouched, rather than being torn down and rebuilt with the calendar it belongs to.
 *
 * Two copies are rendered side by side, both translated by the pager's scroll offset: as the first
 * slides out to the left, the second — sitting exactly one width further right — takes its place,
 * so the row scrolls along with the day columns and loops seamlessly.
 */
@Composable
private fun DayOfWeekOverlayHeader(
    headerWidth: () -> Int,
    updateHeaderWidth: (Int) -> Unit,
    firstDayOfWeek: DayOfWeek,
    headerState: CalendarHeaderState,
    isExpanded: () -> Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Without this the trailing copy would paint outside the calendar while sliding in.
            .clipToBounds()
            .onSizeChanged { updateHeaderWidth(it.width) },
    ) {
        // The offset is read inside `graphicsLayer`, so it is sampled at draw time: the row follows
        // the columns frame for frame, and a scroll never triggers recomposition here.
        DaysOfWeekTitle(
            firstDayOfWeek = firstDayOfWeek,
            modifier = Modifier.graphicsLayer { translationX = headerState.offset(isExpanded()) },
        )
        DaysOfWeekTitle(
            firstDayOfWeek = firstDayOfWeek,
            modifier = Modifier.graphicsLayer { translationX = headerState.offset(isExpanded()) + headerWidth() },
        )
    }
}

@Composable
@Preview
private fun ExpandableCalendarCollapsedPreview() {
    Surface {
        ExpandableCalendar(
            selectedDate = { Clock.today() },
            onDayClick = {},
            isExpanded = { false },
            weekNumbering = WeekNumbering.ISO_8601,
        )
    }
}

@Composable
@Preview
private fun ExpandableCalendarExpandedPreview() {
    Surface {
        ExpandableCalendar(
            selectedDate = { Clock.today() },
            onDayClick = {},
            isExpanded = { true },
            weekNumbering = WeekNumbering.ISO_8601,
        )
    }
}
