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
package com.infomaniak.calendar.components.month.preview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.infomaniak.calendar.components.foundation.models.EventStatus
import com.infomaniak.calendar.components.foundation.models.EventSummaryUi
import com.infomaniak.calendar.components.foundation.models.ParticipationStatus
import com.infomaniak.calendar.components.foundation.models.SimpleEventSummaryUi
import com.infomaniak.calendar.components.foundation.preview.EventColorsUiFactory
import com.infomaniak.calendar.components.foundation.utils.startOfWeek
import com.infomaniak.calendar.components.month.model.MonthEventSegmentUi
import com.infomaniak.calendar.components.month.model.MonthUi
import com.infomaniak.calendar.components.month.model.MonthWeekUi
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import kotlin.time.Instant

/** A month with its first day on a Sunday, so it touches 6 weeks. */
internal val previewSixWeeksMonth = YearMonth(2026, 3)
internal val previewFiveWeeksMonth = YearMonth(2026, 5)

internal val previewEvent: EventSummaryUi.Normal = previewEventOf("Review design Calendar", Color(0xFF8B5CF6))
internal val previewCancelledEvent: EventSummaryUi.Normal = previewEventOf(
    title = "Meeting dev",
    color = Color(0xFFD4426E),
    status = EventStatus.Cancelled,
)
internal val previewDeclinedEvent: EventSummaryUi.Normal = previewEventOf(
    title = "Afterwork",
    color = Color(0xFF10B981),
    myStatus = ParticipationStatus.Declined,
)
internal val previewTentativeEvent: EventSummaryUi.Normal = previewEventOf(
    title = "Dentist",
    color = Color(0xFF3B82F6),
    myStatus = ParticipationStatus.Tentative,
)
internal val previewPendingEvent: EventSummaryUi.Normal = previewEventOf(
    title = "Team lunch",
    color = Color(0xFFE5B94E),
    myStatus = ParticipationStatus.NeedsAction,
)

/**
 * A week with more events on its second day than a short row can show, and events of every status. Only meant for a
 * week fully within its month, as segments never cover days of other months.
 */
internal val previewSegments: List<MonthEventSegmentUi> = listOf(
    MonthEventSegmentUi(previewEventOf("Vacations", Color(0xFFE5B94E)), startColumn = 0, span = 5, lane = 0),
    MonthEventSegmentUi(previewEvent, startColumn = 1, span = 1, lane = 1),
    MonthEventSegmentUi(previewEventOf("Standup", Color(0xFF3B82F6)), startColumn = 1, span = 1, lane = 2),
    MonthEventSegmentUi(previewEventOf("Tennis", Color(0xFF10B981)), startColumn = 1, span = 1, lane = 3),
    MonthEventSegmentUi(previewEventOf("Go running", Color(0xFFD4426E)), startColumn = 1, span = 1, lane = 4),
    MonthEventSegmentUi(previewCancelledEvent, startColumn = 3, span = 1, lane = 1),
    MonthEventSegmentUi(previewDeclinedEvent, startColumn = 4, span = 2, lane = 1),
    MonthEventSegmentUi(previewTentativeEvent, startColumn = 5, span = 1, lane = 0),
    MonthEventSegmentUi(previewPendingEvent, startColumn = 6, span = 1, lane = 0),
)

/** The weeks of [month] from Monday, with [previewSegments] on its second week, the first one fully within it. */
fun previewMonth(month: YearMonth): MonthUi {
    val weekStarts = generateSequence(month.firstDay.startOfWeek(DayOfWeek.MONDAY)) { it.plus(1, DateTimeUnit.WEEK) }
        .takeWhile { it <= month.lastDay }

    val weeks = weekStarts.mapIndexed { index, start ->
        MonthWeekUi(
            dates = List(7) { start.plus(it, DateTimeUnit.DAY) },
            segments = if (index == 1) previewSegments else emptyList(),
        )
    }

    return MonthUi(month, weeks.toList())
}

private fun previewEventOf(
    title: String,
    color: Color,
    status: EventStatus = EventStatus.Confirmed,
    myStatus: ParticipationStatus? = ParticipationStatus.Accepted,
): EventSummaryUi.Normal = SimpleEventSummaryUi(
    id = title,
    title = title,
    location = null,
    status = status,
    start = Instant.DISTANT_PAST,
    end = Instant.DISTANT_PAST,
    isAllDay = true,
    colors = EventColorsUiFactory.dummyEventColorsUiFactory.create(color.toArgb()),
    hasAttendees = myStatus != null,
    myStatus = myStatus,
)
