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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.event.EventItem
import com.infomaniak.calendar.components.eventcard.EventCard
import com.infomaniak.calendar.components.eventcard.EventCardAction
import com.infomaniak.calendar.components.foundation.component.DateState
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.YearWeek
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.DayFormatter.toShortDayName
import com.infomaniak.calendar.components.planning.component.DayIndicator
import com.infomaniak.calendar.components.planning.component.TodayEmptyState
import com.infomaniak.calendar.components.planning.preview.WeekEventsPreviewParameter
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.avatar.models.AvatarColors
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.time.LocalDateTime
import kotlin.time.Clock

private val maxCardSize = 150.dp
private val minCardSize = 72.dp

@Composable
fun Planning(
    weekEvents: () -> Map<YearWeek, Map<LocalDate, List<EventUi>>>,
    goToEventCreation: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    var currentCardSize by remember { mutableStateOf(maxCardSize) }
    val density = LocalDensity.current

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Calculate the change in card size based on scroll delta
                val availableYDp = with(density) { available.y.toDp() }
                val newCardSize = currentCardSize + availableYDp
                val previousCardSize = currentCardSize

                // Constrain the card size within the allowed bounds
                currentCardSize = newCardSize.coerceIn(minCardSize, maxCardSize)
                val consumed = currentCardSize - previousCardSize

                // Return the consumed scroll amount
                return Offset(0f, with(density) { consumed.toPx() })
            }
        }
    }

    Column(
        modifier = modifier.nestedScroll(nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(Margin.Large),
    ) {
        val horizontalContentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
            end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
        )

        val topContentPadding = PaddingValues(top = contentPadding.calculateTopPadding())

        HorizontalPager(
            rememberPagerState { 3 },
            contentPadding = PaddingValues(horizontal = Margin.Medium) + horizontalContentPadding + topContentPadding,
            pageSpacing = Margin.Small,
        ) {
            EventCard(
                modifier = Modifier.height(currentCardSize),
                timeUntilEvent = "In $it minutes",
                title = "Calendar meeting",
                startDate = LocalDateTime.of(2026, 6, 19, 8, 0),
                endDate = LocalDateTime.of(2026, 6, 19, 16, 0),
                location = "Japan room",
                attendees = List(9) { AvatarType.WithInitials.Initials("AB", AvatarColors(Color.Gray, Color.White)) },
                action = EventCardAction.Button.JoinMeeting {},
            )
        }

        Timeline(
            lazyListState = lazyListState,
            weekEvents = weekEvents,
            goToEventCreation = goToEventCreation,
            contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Timeline(
    lazyListState: LazyListState,
    weekEvents: () -> Map<YearWeek, Map<LocalDate, List<EventUi>>>,
    goToEventCreation: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val today = Clock.today()
    val events = weekEvents()
    val sectionSizing = remember { SectionSizing() }

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(Margin.Mini),
    ) {
        events.forEach { (week, days) ->
            item(key = PlanningItemKey.WeekHeader(week.firstDay)) {
                Text(week.label, modifier = Modifier.padding(bottom = Margin.Medium))
            }

            days.forEach { (date, events) ->
                val sectionItemKeys = events.map { it.toItemKey(date) }

                itemsIndexed(events, key = { _, event -> event.toItemKey(date) }) { index, event ->
                    val itemKey = event.toItemKey(date)
                    val bottomPadding = if (index == events.lastIndex) Margin.Medium else 0.dp

                    Row(
                        modifier = Modifier
                            .ensureSectionMinHeight(sectionSizing, sectionItemKeys, itemKey)
                            .padding(bottom = bottomPadding),
                        horizontalArrangement = Arrangement.spacedBy(Margin.Small),
                    ) {
                        DayIndicator(
                            dayName = date.toShortDayName(),
                            dayNumber = date.day,
                            state = if (date == today) DateState.Today else DateState.None,
                            modifier = Modifier
                                .measureIndicator(sectionSizing)
                                .stickyDayIndicator(lazyListState, itemKey, sectionItemKeys),
                        )

                        when (event) {
                            is EventUi.Normal -> EventItem(event, Modifier.fillMaxWidth())
                            is EventUi.TodayEmptyState -> TodayEmptyState(onClick = goToEventCreation, Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

private val YearWeek.label: String
    @Composable get() {
        val week = stringResource(R.string.weekHeaderWeekNumber, weekNumber)
        val dateRange = stringResource(R.string.weekHeaderDateRange, firstDay.day, lastDay.day, monthDisplayName, year)
        return "$week - $dateRange"
    }

private fun EventUi.toItemKey(date: LocalDate): PlanningItemKey = PlanningItemKey.Event(date = date, id = id)

@Preview
@Composable
private fun PreviewPlanning(@PreviewParameter(WeekEventsPreviewParameter::class) weekEvents: Map<YearWeek, Map<LocalDate, List<EventUi>>>) {
    Surface {
        Planning(goToEventCreation = {}, weekEvents = { weekEvents })
    }
}
