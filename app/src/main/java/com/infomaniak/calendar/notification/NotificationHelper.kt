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
package com.infomaniak.calendar.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import androidx.compose.ui.unit.Density
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import com.infomaniak.calendar.MainActivity
import com.infomaniak.calendar.R
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatShortDateRange
import com.infomaniak.calendar.components.foundation.utils.timeFormatter.formatShortDateTimeRange
import com.infomaniak.calendar.utils.toBitmap
import com.infomaniak.core.common.utils.today
import com.infomaniak.designsystem.icons.EsdsIcons
import com.infomaniak.designsystem.icons.symbols.CalendarNumberOne
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import com.infomaniak.calendar.components.resources.R as RComponents

object NotificationHelper {

    const val EXTRA_OCCURRENCE_ID_JSON = "com.infomaniak.calendar.EXTRA_OCCURRENCE_ID_JSON"

    fun buildEventNotification(
        context: Context,
        channelId: String,
        occurrenceIdJson: String,
        title: String,
        location: String?,
        mapsQuery: String?,
        startInstantMs: Long,
        endInstantMs: Long,
        allDayDates: LocalDateRange?,
        kMeetUrl: String?,
        timeZone: TimeZone,
        snoozeIntent: PendingIntent?,
        snoozeDelay: Duration,
    ): Notification {
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            // Unlike its extras, an intent's data is part of the PendingIntent identity, so each occurrence gets its own
            data = "calendar://occurrence/${Uri.encode(occurrenceIdJson)}".toUri()
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OCCURRENCE_ID_JSON, occurrenceIdJson)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val timeText = formatTimingText(context, startInstantMs, endInstantMs, allDayDates, timeZone)
        val contentText = if (!location.isNullOrBlank()) "$timeText • $location" else timeText
        val notificationTitle = title.ifBlank { context.getString(R.string.notificationDefaultEventTitle) }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(IconCompat.createWithBitmap(EsdsIcons.CalendarNumberOne.Medium.toBitmap(Density(context))))
            .setContentTitle(notificationTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        kMeetUrl?.let { builder.addAction(createJoinKMeetAction(context, it)) }
        mapsQuery?.let { builder.addAction(createOpenMapAction(context, it)) }
        snoozeIntent?.let { builder.addAction(createSnoozeAction(context, it, snoozeDelay)) }

        return builder.build()
    }

    private fun createSnoozeAction(context: Context, snoozeIntent: PendingIntent, delay: Duration): NotificationCompat.Action {
        val minutes = delay.inWholeMinutes.toInt()
        val label = context.resources.getQuantityString(R.plurals.notificationActionSnooze, minutes, minutes)
        return NotificationCompat.Action.Builder(null, label, snoozeIntent).build()
    }

    // Opens the meeting straight from the notification: a trampoline through a receiver can't start an activity since Android 12
    private fun createJoinKMeetAction(context: Context, kMeetUrl: String): NotificationCompat.Action {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(Intent.ACTION_VIEW, kMeetUrl.toUri()),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Action.Builder(null, context.getString(RComponents.string.participateKMeetTitle), pendingIntent)
            .build()
    }

    // Opens the maps app straight from the notification: a trampoline through a receiver can't start an activity since Android 12
    private fun createOpenMapAction(context: Context, mapsQuery: String): NotificationCompat.Action {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(Intent.ACTION_VIEW, "geo:0,0?q=${Uri.encode(mapsQuery)}".toUri()),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Action.Builder(null, context.getString(R.string.notificationActionOpenMap), pendingIntent).build()
    }

    private fun formatTimingText(
        context: Context,
        startInstantMs: Long,
        endInstantMs: Long,
        allDayDates: LocalDateRange?,
        timeZone: TimeZone,
    ): String {
        val locale = context.resources.configuration.locales[0]
        val today = Clock.today(timeZone)

        if (allDayDates != null) {
            val firstDay = allDayDates.start
            if (firstDay == allDayDates.endInclusive && firstDay == today) return context.getString(RComponents.string.notificationAllDay)
            return formatShortDateRange(firstDay, allDayDates.endInclusive, locale, today.year)
        }

        val start = Instant.fromEpochMilliseconds(startInstantMs).toLocalDateTime(timeZone)
        val end = Instant.fromEpochMilliseconds(endInstantMs).toLocalDateTime(timeZone)
        return formatShortDateTimeRange(start, end, locale, DateFormat.is24HourFormat(context), today)
    }
}
