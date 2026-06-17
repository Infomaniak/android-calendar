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

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.event.EventItem
import com.infomaniak.calendar.components.models.DateState
import com.infomaniak.calendar.components.models.EventUi
import com.infomaniak.calendar.components.models.LocalDateParceler
import com.infomaniak.calendar.components.models.YearWeek
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.todayIn
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.time.format.DateTimeFormatter
import kotlin.time.Clock

private val shortDayNameFormatter = DateTimeFormatter.ofPattern("EEE")

@Composable
fun Planning(
    weekEvents: () -> Map<YearWeek, Map<LocalDate, List<EventUi>>>,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
    ) {
        weekEvents().forEach { (week, days) ->
            item(key = week) { Text(week.label) }

            days.forEach { (date, events) ->
                val dayKey = DayKey(date)
                item(key = dayKey) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Margin.Small)) {
                        DayIndicator(
                            dayName = date.toJavaLocalDate().format(shortDayNameFormatter),
                            dayNumber = date.day,
                            state = if (date == today) DateState.Today else DateState.None,
                            modifier = Modifier
                                .padding(start = Margin.Medium)
                                .stickyWithinItem(lazyListState, dayKey),
                        )
                        EventList(events, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayIndicator(dayName: String, dayNumber: Int, state: DateState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(dayName, style = MaterialTheme.typography.bodyMedium)

        Box(
            modifier = Modifier
                .size(48.dp)
                .padding(Margin.Micro)
                .background(state.containerColor(), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = dayNumber.toString(),
                color = state.contentColor(),
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun EventList(events: List<EventUi>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        events.forEach { event ->
            EventItem(event)
        }
    }
}

@Parcelize
@TypeParceler<LocalDate, LocalDateParceler>
private data class DayKey(val date: LocalDate) : Parcelable

private val YearWeek.label: String
    get() {
        val monthName = month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "Week $weekNumber - ${firstDay.day} - ${lastDay.day} $monthName $year"
    }


@Preview
@Composable
private fun PreviewPlanning() {
    Surface {
        Planning(weekEvents = { previewWeekEvents })
    }
}
