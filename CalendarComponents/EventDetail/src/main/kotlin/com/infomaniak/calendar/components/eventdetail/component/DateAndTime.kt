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
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDateRange
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDateTimeRange
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatDateTimeRangeWithZone
import com.infomaniak.calendar.components.resources.R
import com.infomaniak.core.common.utils.today
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.atTime
import kotlinx.datetime.offsetAt
import kotlin.time.Clock

@Composable
internal fun DateAndTime(
    startAtLocale: @Composable () -> LocalDateTime,
    endAtLocale: @Composable () -> LocalDateTime,
    startUtcOffsetAtLocale: @Composable () -> UtcOffset?,
    endUtcOffsetAtLocale: @Composable () -> UtcOffset?,
    startAtTimeZone: LocalDateTime,
    endAtTimeZone: LocalDateTime,
    startUtcOffsetAtTimeZone: @Composable () -> UtcOffset?,
    endUtcOffsetAtTimeZone: @Composable () -> UtcOffset?,
    isAllDay: Boolean,
    modifier: Modifier = Modifier,
) {
    ListItem(
        leadingContent = { Icon(painterResource(R.drawable.ic_clock), contentDescription = null) },
        headlineContent = {
            val startLocale = startAtLocale()
            val endLocale = endAtLocale()
            Text(
                text = if (isAllDay) {
                    formatDateRange(startLocale.date, endLocale.date)
                } else {
                    formatDateTimeRange(startLocale, endLocale)
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            val startUtcOffsetAtTimeZone = startUtcOffsetAtTimeZone()
            val endUtcOffsetAtTimeZone = endUtcOffsetAtTimeZone()

            // Floating events and all day events never need to display UTC offset
            if (startUtcOffsetAtTimeZone == null || endUtcOffsetAtTimeZone == null || isAllDay) return@ListItem

            if (startUtcOffsetAtTimeZone != startUtcOffsetAtLocale() || endUtcOffsetAtTimeZone != endUtcOffsetAtLocale()) {
                Text(
                    text = formatDateTimeRangeWithZone(
                        start = startAtTimeZone,
                        startUtcOffset = startUtcOffsetAtTimeZone,
                        end = endAtTimeZone,
                        endUtcOffset = endUtcOffsetAtTimeZone,
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
    val parisTimeZone = TimeZone.of("Europe/Paris")
    val parisUtcOffsetToday = parisTimeZone.offsetAt(Clock.System.now())
    val tokyoTimeZone = TimeZone.of("Asia/Tokyo")
    val tokyoUtcOffsetToday = tokyoTimeZone.offsetAt(Clock.System.now())
    val startDate = LocalDateTime.parse("2026-05-20T08:00:00")
    val endDate = LocalDateTime.parse("2026-05-20T09:00:00")

    @Composable
    fun DateAndTimeComponent(
        startUtcOffset: UtcOffset? = UtcOffset.ZERO,
        endUtcOffset: UtcOffset? = UtcOffset.ZERO,
        startUtcOffsetAtLocale: UtcOffset? = UtcOffset.ZERO,
        endUtcOffsetAtLocale: UtcOffset? = UtcOffset.ZERO,
        start: LocalDateTime = startDate,
        end: LocalDateTime = endDate,
        isAllDay: Boolean = false,
    ) {
        DateAndTime(
            startAtLocale = { start },
            endAtLocale = { end },
            startAtTimeZone = start,
            endAtTimeZone = end,
            startUtcOffsetAtTimeZone = { startUtcOffset },
            endUtcOffsetAtTimeZone = { endUtcOffset },
            startUtcOffsetAtLocale = { startUtcOffsetAtLocale },
            endUtcOffsetAtLocale = { endUtcOffsetAtLocale },
            isAllDay = isAllDay,
        )
    }

    MaterialTheme {
        Surface {
            Column {
                DateAndTimeComponent(start = Clock.today().atTime(8, 0), end = Clock.today().atTime(9, 0))
                DateAndTimeComponent(
                    start = Clock.today().atTime(8, 0),
                    end = Clock.today().atTime(9, 0),
                    startUtcOffset = parisUtcOffsetToday,
                )
                DateAndTimeComponent(startUtcOffset = null, endUtcOffset = null)
                DateAndTimeComponent(parisUtcOffsetToday, tokyoUtcOffsetToday)
                DateAndTimeComponent(parisUtcOffsetToday, parisUtcOffsetToday, isAllDay = true)
                DateAndTimeComponent(
                    startUtcOffset = parisUtcOffsetToday,
                    endUtcOffset = parisUtcOffsetToday,
                    end = LocalDateTime.parse("2026-05-22T09:00:00"),
                    isAllDay = true,
                )
            }
        }
    }
}
