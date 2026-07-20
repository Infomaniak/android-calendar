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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.event.EventItem
import com.infomaniak.calendar.components.eventcard.EventCard
import com.infomaniak.calendar.components.eventcard.EventCardAction
import com.infomaniak.calendar.components.eventcard.rememberEventCardState
import com.infomaniak.calendar.components.foundation.component.DateState
import com.infomaniak.calendar.components.foundation.models.AttendeeUi
import com.infomaniak.calendar.components.foundation.models.Attendees
import com.infomaniak.calendar.components.foundation.models.EventUi
import com.infomaniak.calendar.components.foundation.models.YearWeek
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.DayFormatter.toShortDayName
import com.infomaniak.calendar.components.planning.component.DayIndicator
import com.infomaniak.calendar.components.planning.component.TodayEmptyState
import com.infomaniak.calendar.components.planning.preview.WeekEventsPreviewParameter
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.avatar.LocalAvatarColors
import com.infomaniak.core.avatar.getBackgroundColorResBasedOnId
import com.infomaniak.core.avatar.models.AvatarColors
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.common.utils.today
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

@Composable
fun Planning(
    weekEvents: () -> Map<YearWeek, Map<LocalDate, List<EventUi>>>,
    nextEvents: () -> List<EventUi.Normal>,
    goToEventCreation: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val eventCardState = rememberEventCardState()
    var currentCardSize by remember { mutableStateOf<Dp?>(null) }
    val density = LocalDensity.current

    val nestedScrollConnection = remember(density) {
        CardNestedScrollConnection(
            eventCardState = eventCardState,
            currentCardSize = { currentCardSize },
            updateCurrentCardSize = { coercedNewCardSize -> currentCardSize = coercedNewCardSize },
            density = density,
        )
    }

    Box(modifier = modifier.nestedScroll(nestedScrollConnection)) {
        val horizontalContentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
            end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
        )
        val topContentPadding = contentPadding.calculateTopPadding()

        Timeline(
            lazyListState = lazyListState,
            weekEvents = weekEvents,
            goToEventCreation = goToEventCreation,
            contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding(), top = topContentPadding)
                    + horizontalContentPadding
                    + PaddingValues(top = (currentCardSize ?: eventCardState.initialHeightDp ?: 0.dp) + Margin.Medium),
            modifier = Modifier.fillMaxSize(),
        )

        val nextEventCards = nextEvents()
        HorizontalPager(
            state = rememberPagerState { nextEventCards.size },
            contentPadding = horizontalContentPadding,
            pageSpacing = Margin.Small,
            modifier = Modifier.padding(top = topContentPadding),
        ) { page ->
            val event = nextEventCards[page]
            EventCard(
                title = event.title,
                startDate = event.start,
                endDate = event.end,
                location = event.location,
                attendees = event.attendees.toAvatarTypes(),
                action = EventCardAction.None,
                progress = { eventCardState.computeProgress(currentCardSize, density) },
                modifier = Modifier.fillMaxWidth(),
                eventCardState = eventCardState,
                shape = MaterialTheme.shapes.large,
            )
        }
    }
}

@Composable
private fun Attendees.toAvatarTypes(): List<AvatarType> {
    val avatarColors = LocalAvatarColors.current

    return all.map { attendee ->
        val attendeeId = 31 * attendee.email.hashCode() + attendee.displayName.hashCode()

        AvatarType.getUrlOrInitials(
            avatarUrlData = null,
            initials = attendee.initials(),
            colors = AvatarColors(
                containerColor = getBackgroundColorResBasedOnId(attendeeId, avatarColors.containerColors),
                contentColor = avatarColors.contentColor,
            ),
        )
    }
}

private fun AttendeeUi.initials(): String {
    val source = displayName?.takeIf { it.isNotBlank() } ?: email
    return source.trim().split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }
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
        Planning(
            goToEventCreation = {},
            weekEvents = { weekEvents },
            nextEvents = { weekEvents.nextEventsPreview() },
        )
    }
}

private fun Map<YearWeek, Map<LocalDate, List<EventUi>>>.nextEventsPreview(): List<EventUi.Normal> {
    val upcoming = values.asSequence()
        .flatMap { days -> days.values.asSequence().flatten() }
        .filterIsInstance<EventUi.Normal>()
        .filter { it.start >= Clock.System.now() }
        .toList()

    val nextStart = upcoming.minOfOrNull { it.start } ?: return emptyList()
    return upcoming.filter { it.start == nextStart }
}
