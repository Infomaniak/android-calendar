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
package com.infomaniak.calendar.components.eventdetail.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.calendar.components.eventdetail.models.EventDetailTiming
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDateRange
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDateTimeRange
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDateTimeRangeWithZone
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.time.Clock

@Composable
internal fun DateAndTime(
    start: EventDetailTiming,
    end: EventDetailTiming,
    isAllDay: Boolean,
    modifier: Modifier = Modifier,
) {
    ListItem(
        leadingContent = { Icon(painterResource(R.drawable.ic_clock), contentDescription = null) },
        headlineContent = {
            val startAtLocale = start.atLocale
            val endAtLocale = end.atLocale
            Text(
                text = if (isAllDay) {
                    formatDateRange(startAtLocale.date, endAtLocale.date)
                } else {
                    formatDateTimeRange(startAtLocale, endAtLocale)
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            // Floating events and all day events never need to display UTC offset
            if (isAllDay || start !is EventDetailTiming.Precised || end !is EventDetailTiming.Precised) return@ListItem

            if (start.utcOffsetAtTimeZone != start.utcOffsetAtLocale || end.utcOffsetAtTimeZone != end.utcOffsetAtLocale) {
                Text(
                    text = formatDateTimeRangeWithZone(
                        start = start.atTimeZone,
                        startUtcOffset = start.utcOffsetAtTimeZone,
                        end = end.atTimeZone,
                        endUtcOffset = end.utcOffsetAtTimeZone,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun PreviewDateAndTime() {
    val localTimeZone = TimeZone.currentSystemDefault()
    val parisTimeZone = TimeZone.of("Europe/Paris")
    val tokyoTimeZone = TimeZone.of("Asia/Tokyo")
    val today = LocalDateTime.parse("2026-05-20T08:00:00")
    val laterToday = LocalDateTime.parse("2026-05-20T09:00:00")
    val tomorrow = LocalDateTime.parse("2026-05-21T09:00:00")

    // A null time zone represents a floating (timezone-less) side of the event.
    @Composable
    fun DateAndTimeComponent(
        startTimeZone: TimeZone? = localTimeZone,
        endTimeZone: TimeZone? = localTimeZone,
        start: LocalDateTime = today,
        end: LocalDateTime = laterToday,
        isAllDay: Boolean = false,
    ) {
        DateAndTime(
            start = if (isAllDay || startTimeZone == null) {
                EventDetailTiming.Floating(start)
            } else {
                EventDetailTiming.Precised(start.toInstant(startTimeZone), startTimeZone)
            },
            end = if (isAllDay || endTimeZone == null) {
                EventDetailTiming.Floating(end)
            } else {
                EventDetailTiming.Precised(end.toInstant(endTimeZone), endTimeZone)
            },
            isAllDay = isAllDay,
        )
    }

    @Composable
    fun PreviewSectionHeadline(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        )
    }

    MaterialTheme {
        Surface {
            Column {
                PreviewSectionHeadline("Same day")
                DateAndTimeComponent(start = Clock.today().atTime(8, 0), end = Clock.today().atTime(9, 0))
                DateAndTimeComponent(
                    start = Clock.today().atTime(8, 0),
                    end = Clock.today().atTime(9, 0),
                    startTimeZone = tokyoTimeZone,
                )

                PreviewSectionHeadline("Special timezones")
                DateAndTimeComponent(startTimeZone = null, endTimeZone = null)
                DateAndTimeComponent(startTimeZone = parisTimeZone, endTimeZone = tokyoTimeZone)

                PreviewSectionHeadline("Multi days")
                DateAndTimeComponent(end = tomorrow)
                DateAndTimeComponent(endTimeZone = tokyoTimeZone)

                PreviewSectionHeadline("All day")
                DateAndTimeComponent(startTimeZone = parisTimeZone, endTimeZone = parisTimeZone, isAllDay = true)
                DateAndTimeComponent(
                    startTimeZone = parisTimeZone,
                    endTimeZone = parisTimeZone,
                    end = LocalDateTime.parse("2026-05-22T09:00:00"),
                    isAllDay = true,
                )
            }
        }
    }
}
